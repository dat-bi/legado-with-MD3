package io.legado.app.help.translate

import android.util.Log
import io.legado.app.help.book.BookContent
import io.legado.app.help.config.AppConfig
import kotlinx.coroutines.delay

object TranslateManager {
    
    private const val TAG = "TranslateManager"
    private val apiService = TranslateApiService()
    
    suspend fun translateIfEnabled(title: String, content: String): Pair<String, String> {
        val config = AppConfig.buildTranslateConfig()
        
        if (!config.enabled) {
            return title to content
        }
        
        return try {
            val translatedTitle = translateText(title, config)
            delay(config.delayMs)
            val translatedContent = translateText(content, config)
            translatedTitle to translatedContent
        } catch (e: Exception) {
            Log.e(TAG, "Translation failed", e)
            title to content
        }
    }
    
    suspend fun translateIfEnabled(title: String, content: BookContent): Pair<String, BookContent> {
        val config = AppConfig.buildTranslateConfig()
        
        if (!config.enabled) {
            return title to content
        }
        
        return try {
            val translatedTitle = translateText(title, config)
            delay(config.delayMs)
            val contentStr = content.toString()
            val translatedContent = translateText(contentStr, config)
            // Split translated content back into list of strings
            val translatedTextList = translatedContent.split("\n")
            val translatedBookContent = content.copy(textList = translatedTextList)
            translatedTitle to translatedBookContent
        } catch (e: Exception) {
            Log.e(TAG, "Translation failed", e)
            title to content
        }
    }
    
    suspend fun translateText(text: String, config: TranslateConfig): String {
        if (text.isBlank() || text.length > 5000) {
            return text
        }
        
        // Don't translate if already contains Vietnamese characters
        if (text.contains(Regex("[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ]"))) {
            return text
        }
        
        // Check cache first
        val cacheKey = "${config.provider.name}_${text.hashCode()}"
        TranslateCache.get(cacheKey)?.let { cached ->
            return cached
        }
        
        val result = try {
            when (config.provider) {
                TranslateProvider.DICHNGAY -> apiService.translateWithDichngay(text, config.dichngayEndpoint)
                TranslateProvider.DICHNHANH -> apiService.translateWithDichnhanh(
                    text = text,
                    endpoint = config.dichnhanhEndpoint,
                    mode = config.dichnhanhMode,
                    type = config.dichnhanhType,
                    enableAnalyze = config.dichnhanhEnableAnalyze,
                    enableFanfic = config.dichnhanhEnableFanfic
                )
                TranslateProvider.SANGTACVIET -> apiService.translateWithSangtacviet(text, config.sangtacvietEndpoint)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Translation error for text: ${text.take(50)}", e)
            text
        }
        
        // Cache the result if translation actually changed the text
        if (result != text && result.isNotEmpty()) {
            TranslateCache.put(cacheKey, result)
            Log.d(TAG, "Translated: ${text.take(30)} -> ${result.take(30)}")
        }
        
        return result
    }
    
    suspend fun translateList(texts: List<String>, config: TranslateConfig): List<String> {
        return texts.map { text ->
            translateText(text, config)
        }
    }
    
    suspend fun translateSingle(text: String): String {
        val config = AppConfig.buildTranslateConfig()
        return translateText(text, config)
    }
}
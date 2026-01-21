package io.legado.app.help.translate

import android.util.Log
import io.legado.app.help.http.okHttpClient
import io.legado.app.help.http.newCallStrResponse
import io.legado.app.utils.GSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class TranslateApiService {
    
    private companion object {
        private const val TAG = "TranslateApiService"
    }
    
    /**
     * Translate text using Dichngay API (dichngay.com)
     * Same method as the TM Translate script - sends JSON POST
     */
    suspend fun translateWithDichngay(text: String, endpoint: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Check cache first
                if (text.isBlank() || text.length > 5000) {
                    return@withContext text
                }
                
                // Send as JSON payload to dichngay - using array format like the script
                val payload = mapOf(
                    "content" to GSON.toJson(listOf(text)),
                    "tl" to "vi"
                )
                
                val requestBody = GSON.toJson(payload).toRequestBody("application/json".toMediaType())
                
                val response = okHttpClient.newCallStrResponse {
                    url(endpoint)
                    header("referer", "https://dichngay.com/")
                    post(requestBody)
                }
                
                val responseBody = response.body ?: return@withContext text
                if (responseBody.isEmpty()) return@withContext text
                
                try {
                    val jsonResponse = JSONObject(responseBody)
                    val contentData = jsonResponse.optString("data", "")
                    if (contentData.isNotEmpty()) {
                        // Try to parse as JSON array
                        try {
                            val jsonArray = org.json.JSONArray(contentData)
                            if (jsonArray.length() > 0) {
                                return@withContext jsonArray.getString(0)
                            }
                        } catch (e: Exception) {
                            // If not array, return as string
                            return@withContext contentData
                        }
                    }
                    
                    val translatedText = jsonResponse.optString("translatedText", "")
                    if (translatedText.isNotEmpty()) return@withContext translatedText
                    
                    text
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing dichngay response", e)
                    text
                }
            } catch (e: Exception) {
                Log.e(TAG, "Dichngay translation error", e)
                text
            }
        }
    }
    
    /**
     * Translate text using Dictnhanh API (api.dictnhanh.com)
     * Uses FormData with specific parameters like the script
     */
    suspend fun translateWithDichnhanh(
        text: String,
        endpoint: String,
        mode: String,
        type: String,
        enableAnalyze: Boolean,
        enableFanfic: Boolean
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                if (text.isBlank() || text.length > 5000) {
                    return@withContext text
                }
                
                // Format: serialize as JSON array then send
                val serializedTexts = GSON.toJson(listOf(text))
                
                val params = mapOf(
                    "type" to if (type == "Modern") "Modern" else "Ancient",
                    "enable_analyze" to if (enableAnalyze) "1" else "0",
                    "enable_fanfic" to if (enableFanfic) "1" else "0",
                    "mode" to (if (mode in listOf("vi", "qt", "hv")) mode else "vi"),
                    "text" to serializedTexts,
                    "remove" to ""
                )
                
                // Convert to URL encoded form
                val formBody = params.entries.joinToString("&") { (k, v) ->
                    "${k}=${java.net.URLEncoder.encode(v, "UTF-8")}"
                }.toRequestBody("application/x-www-form-urlencoded".toMediaType())
                
                val response = okHttpClient.newCallStrResponse {
                    url(endpoint)
                    header("referer", "https://dichnhanh.com/")
                    header("Accept", "application/json, text/plain, */*")
                    post(formBody)
                }
                
                val responseBody = response.body ?: return@withContext text
                if (responseBody.isEmpty()) return@withContext text
                
                try {
                    val jsonResponse = JSONObject(responseBody)
                    
                    if (jsonResponse.optBoolean("success", false)) {
                        val data = jsonResponse.optJSONObject("data")
                        val rawContent = data?.optString("content", "")
                        
                        if (!rawContent.isNullOrEmpty()) {
                            try {
                                // Sanitize and parse
                                val sanitized = rawContent
                                    .replace("\\\"", "\"")
                                    .trim()
                                val jsonArray = org.json.JSONArray("[$sanitized]")
                                if (jsonArray.length() > 0) {
                                    return@withContext jsonArray.getString(0)
                                }
                            } catch (e: Exception) {
                                return@withContext rawContent
                            }
                        }
                    }
                    
                    text
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing dictnhanh response", e)
                    text
                }
            } catch (e: Exception) {
                Log.e(TAG, "Dictnhanh translation error", e)
                text
            }
        }
    }
    
    /**
     * Translate text using Sangtacviet API
     * Format: POST form-urlencoded with ajax=trans, content, nonodes=true
     * Response: Text with ||| separators that should be converted to newlines
     */
    suspend fun translateWithSangtacviet(text: String, endpoint: String): String {
        return withContext(Dispatchers.IO) {
            try {
                if (text.isBlank() || text.length > 5000) {
                    return@withContext text
                }
                
                // Build form body: ajax=trans&content=<text>&nonodes=true
                val formBody = FormBody.Builder()
                    .add("ajax", "trans")
                    .add("content", text)
                    .add("nonodes", "true")
                    .build()
                
                val response = okHttpClient.newCallStrResponse {
                    url(endpoint)
                    header("Content-type", "application/x-www-form-urlencoded")
                    post(formBody)
                }
                
                val responseBody = response.body ?: return@withContext text
                if (responseBody.isNotEmpty()) {
                    // Response contains ||| separators - replace with newlines and trim
                    responseBody.split("|||").joinToString("\n").trim()
                } else {
                    text
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sangtacviet translation error", e)
                // Fallback to MyMemory if sangtacviet fails
                translateByMyMemory(text)
            }
        }
    }
    
    /**
     * Translate using MyMemory API (free, no key required, most reliable)
     */
    private suspend fun translateByMyMemory(text: String): String {
        return try {
            if (text.isBlank() || text.length > 500) {
                text
            } else {
                val encodedText = java.net.URLEncoder.encode(text, "UTF-8")
                val url = "https://api.mymemory.translated.net/get?q=$encodedText&langpair=en|vi"
                
                val response = okHttpClient.newCallStrResponse {
                    url(url)
                    get()
                }
                
                val body = response.body
                if (!body.isNullOrEmpty()) {
                    val json = JSONObject(body)
                    val responseData = json.optJSONObject("responseData")
                    responseData?.optString("translatedText", "")?.trim()?.ifEmpty { text } ?: text
                } else {
                    text
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "MyMemory translation error", e)
            text
        }
    }
}

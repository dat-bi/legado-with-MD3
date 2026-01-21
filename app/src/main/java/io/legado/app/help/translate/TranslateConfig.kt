package io.legado.app.help.translate

data class TranslateConfig(
    val enabled: Boolean = false,
    val provider: TranslateProvider = TranslateProvider.DICHNGAY,
    val targetLang: String = "vi",
    val delayMs: Long = 400,
    val maxCharsPerRequest: Int = 4500,
    val dichngayEndpoint: String = "https://dichngay.com/translate/text",
    val dichnhanhEndpoint: String = "https://api.dichnhanh.com/",
    val dichnhanhMode: String = "vi",
    val dichnhanhType: String = "Ancient",
    val dichnhanhEnableAnalyze: Boolean = false,
    val dichnhanhEnableFanfic: Boolean = false,
    val sangtacvietEndpoint: String = "http://14.225.254.182/index.php?ngmar=trans&langhint=chinese"
)
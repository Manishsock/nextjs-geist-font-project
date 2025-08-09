package com.chals.ai.assistant.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.chals.ai.assistant.utils.EmotionDetector
import java.util.Locale

class TextToSpeechService : Service(), TextToSpeech.OnInitListener {
    
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var pendingText: String? = null
    private var pendingLanguage: String? = null
    private var pendingEmotion: String? = null
    
    companion object {
        private const val TAG = "TextToSpeechService"
        private const val UTTERANCE_ID = "chals_speech"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "TextToSpeechService created")
        initializeTextToSpeech()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val textToSpeak = intent?.getStringExtra("text_to_speak") ?: ""
        val language = intent?.getStringExtra("language") ?: "en"
        val emotion = intent?.getStringExtra("emotion") ?: "NEUTRAL"
        
        Log.d(TAG, "Speaking text: $textToSpeak (Language: $language, Emotion: $emotion)")
        
        if (isInitialized) {
            speakText(textToSpeak, language, emotion)
        } else {
            // Store for later when TTS is initialized
            pendingText = textToSpeak
            pendingLanguage = language
            pendingEmotion = emotion
        }
        
        return START_NOT_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "TextToSpeechService destroyed")
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(this, this)
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "TextToSpeech initialized successfully")
            isInitialized = true
            
            // Set up utterance progress listener
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d(TAG, "Speech started")
                    val intent = Intent("com.chals.ai.assistant.TTS_STARTED")
                    sendBroadcast(intent)
                }
                
                override fun onDone(utteranceId: String?) {
                    Log.d(TAG, "Speech completed")
                    val intent = Intent("com.chals.ai.assistant.TTS_COMPLETED")
                    sendBroadcast(intent)
                    stopSelf()
                }
                
                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "Speech error")
                    val intent = Intent("com.chals.ai.assistant.TTS_ERROR")
                    sendBroadcast(intent)
                    stopSelf()
                }
            })
            
            // Speak pending text if any
            if (pendingText != null) {
                speakText(pendingText!!, pendingLanguage ?: "en", pendingEmotion ?: "NEUTRAL")
                pendingText = null
                pendingLanguage = null
                pendingEmotion = null
            }
            
        } else {
            Log.e(TAG, "TextToSpeech initialization failed")
            stopSelf()
        }
    }
    
    private fun speakText(text: String, language: String, emotion: String) {
        try {
            // Set language
            val locale = getLocaleForLanguage(language)
            val result = textToSpeech?.setLanguage(locale)
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Language not supported: $language, falling back to English")
                textToSpeech?.setLanguage(Locale.ENGLISH)
            }
            
            // Configure speech parameters based on emotion
            configureSpeechForEmotion(emotion)
            
            // Speak the text
            val params = HashMap<String, String>()
            params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = UTTERANCE_ID
            
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params)
            
            Log.d(TAG, "Started speaking: $text")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in speakText: ${e.message}")
            stopSelf()
        }
    }
    
    private fun getLocaleForLanguage(language: String): Locale {
        return when (language) {
            "hi" -> Locale("hi", "IN") // Hindi (India)
            "hi-en", "hinglish" -> Locale("en", "IN") // English (India) for Hinglish
            else -> Locale.ENGLISH // Default to English
        }
    }
    
    private fun configureSpeechForEmotion(emotion: String) {
        try {
            val emotionEnum = EmotionDetector.Emotion.valueOf(emotion)
            
            when (emotionEnum) {
                EmotionDetector.Emotion.HAPPY -> {
                    // Slightly faster and higher pitch for happiness
                    textToSpeech?.setSpeechRate(1.1f)
                    textToSpeech?.setPitch(1.1f)
                }
                EmotionDetector.Emotion.SAD -> {
                    // Slower and lower pitch for sadness
                    textToSpeech?.setSpeechRate(0.8f)
                    textToSpeech?.setPitch(0.9f)
                }
                EmotionDetector.Emotion.EXCITED -> {
                    // Faster and higher pitch for excitement
                    textToSpeech?.setSpeechRate(1.2f)
                    textToSpeech?.setPitch(1.2f)
                }
                EmotionDetector.Emotion.ANGRY -> {
                    // Slightly faster with normal pitch for anger
                    textToSpeech?.setSpeechRate(1.0f)
                    textToSpeech?.setPitch(1.0f)
                }
                EmotionDetector.Emotion.TIRED -> {
                    // Slower and slightly lower pitch for tiredness
                    textToSpeech?.setSpeechRate(0.9f)
                    textToSpeech?.setPitch(0.95f)
                }
                EmotionDetector.Emotion.NEUTRAL -> {
                    // Normal speech parameters
                    textToSpeech?.setSpeechRate(1.0f)
                    textToSpeech?.setPitch(1.0f)
                }
            }
            
            Log.d(TAG, "Configured speech for emotion: $emotion")
            
        } catch (e: Exception) {
            Log.w(TAG, "Unknown emotion: $emotion, using neutral settings")
            textToSpeech?.setSpeechRate(1.0f)
            textToSpeech?.setPitch(1.0f)
        }
    }
    
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking ?: false
    }
    
    fun stopSpeaking() {
        textToSpeech?.stop()
        Log.d(TAG, "Speech stopped")
    }
    
    // Method to get available languages
    fun getAvailableLanguages(): Set<Locale>? {
        return textToSpeech?.availableLanguages
    }
    
    // Method to check if a language is supported
    fun isLanguageSupported(locale: Locale): Boolean {
        val result = textToSpeech?.isLanguageAvailable(locale)
        return result == TextToSpeech.LANG_AVAILABLE || 
               result == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
               result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
    }
}

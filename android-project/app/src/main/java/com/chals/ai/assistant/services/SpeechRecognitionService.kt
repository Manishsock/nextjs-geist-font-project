package com.chals.ai.assistant.services

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.chals.ai.assistant.utils.LanguageDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

class SpeechRecognitionService : Service(), RecognitionListener {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var languageDetector: LanguageDetector
    
    companion object {
        private const val TAG = "SpeechRecognitionService"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SpeechRecognitionService created")
        languageDetector = LanguageDetector()
        initializeSpeechRecognizer()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting speech recognition...")
        startListening()
        return START_NOT_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SpeechRecognitionService destroyed")
        stopListening()
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(this)
        } else {
            Log.e(TAG, "Speech recognition not available on this device")
        }
    }
    
    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            
            // Support for Hindi and English
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN,en-US")
            putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayOf("hi-IN", "en-US"))
        }
        
        try {
            speechRecognizer?.startListening(intent)
            Log.d(TAG, "Speech recognition started")
            
            // Broadcast that we're listening
            val broadcastIntent = Intent("com.chals.ai.assistant.SPEECH_LISTENING_STARTED")
            sendBroadcast(broadcastIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition: ${e.message}")
        }
    }
    
    private fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
    
    // RecognitionListener implementation
    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "Ready for speech")
    }
    
    override fun onBeginningOfSpeech() {
        Log.d(TAG, "Beginning of speech detected")
    }
    
    override fun onRmsChanged(rmsdB: Float) {
        // Audio level changed - can be used for voice indicator animation
        val broadcastIntent = Intent("com.chals.ai.assistant.AUDIO_LEVEL_CHANGED")
        broadcastIntent.putExtra("audio_level", rmsdB)
        sendBroadcast(broadcastIntent)
    }
    
    override fun onBufferReceived(buffer: ByteArray?) {
        // Audio buffer received
    }
    
    override fun onEndOfSpeech() {
        Log.d(TAG, "End of speech detected")
    }
    
    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
        
        Log.e(TAG, "Speech recognition error: $errorMessage")
        
        // Broadcast error
        val broadcastIntent = Intent("com.chals.ai.assistant.SPEECH_RECOGNITION_ERROR")
        broadcastIntent.putExtra("error_message", errorMessage)
        sendBroadcast(broadcastIntent)
        
        // Stop the service
        stopSelf()
    }
    
    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val recognizedText = matches[0]
            Log.d(TAG, "Speech recognized: $recognizedText")
            
            serviceScope.launch {
                processRecognizedSpeech(recognizedText)
            }
        }
    }
    
    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val partialText = matches[0]
            Log.d(TAG, "Partial speech: $partialText")
            
            // Broadcast partial results for real-time UI updates
            val broadcastIntent = Intent("com.chals.ai.assistant.PARTIAL_SPEECH_RESULT")
            broadcastIntent.putExtra("partial_text", partialText)
            sendBroadcast(broadcastIntent)
        }
    }
    
    override fun onEvent(eventType: Int, params: Bundle?) {
        // Handle speech recognition events
    }
    
    private suspend fun processRecognizedSpeech(text: String) {
        try {
            // Detect language (Hindi, English, or Hinglish)
            val detectedLanguage = languageDetector.detectLanguage(text)
            Log.d(TAG, "Detected language: $detectedLanguage")
            
            // Broadcast the final recognized text
            val broadcastIntent = Intent("com.chals.ai.assistant.SPEECH_RECOGNIZED")
            broadcastIntent.putExtra("recognized_text", text)
            broadcastIntent.putExtra("detected_language", detectedLanguage)
            sendBroadcast(broadcastIntent)
            
            // Start AI processing
            val aiIntent = Intent(this, AIProcessingService::class.java)
            aiIntent.putExtra("user_input", text)
            aiIntent.putExtra("language", detectedLanguage)
            startService(aiIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing recognized speech: ${e.message}")
        } finally {
            // Stop this service
            stopSelf()
        }
    }
}

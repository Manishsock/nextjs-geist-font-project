package com.chals.ai.assistant.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.chals.ai.assistant.api.OpenAIClient
import com.chals.ai.assistant.utils.LanguageDetector
import com.chals.ai.assistant.utils.EmotionDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AIProcessingService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var openAIClient: OpenAIClient
    private lateinit var emotionDetector: EmotionDetector
    
    companion object {
        private const val TAG = "AIProcessingService"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AIProcessingService created")
        
        openAIClient = OpenAIClient()
        emotionDetector = EmotionDetector()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userInput = intent?.getStringExtra("user_input") ?: ""
        val language = intent?.getStringExtra("language") ?: "en"
        
        Log.d(TAG, "Processing user input: $userInput (Language: $language)")
        
        serviceScope.launch {
            processUserInput(userInput, language)
        }
        
        return START_NOT_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "AIProcessingService destroyed")
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    private suspend fun processUserInput(userInput: String, language: String) {
        try {
            // Broadcast processing started
            val processingIntent = Intent("com.chals.ai.assistant.AI_PROCESSING_STARTED")
            sendBroadcast(processingIntent)
            
            // Detect emotion from text
            val detectedEmotion = emotionDetector.detectEmotionFromText(userInput)
            Log.d(TAG, "Detected emotion: $detectedEmotion")
            
            // Create system prompt based on language and emotion
            val systemPrompt = createSystemPrompt(language, detectedEmotion)
            
            // Get AI response
            val aiResponse = openAIClient.getChatCompletion(
                systemPrompt = systemPrompt,
                userMessage = userInput,
                language = language
            )
            
            Log.d(TAG, "AI Response: $aiResponse")
            
            // Broadcast AI response
            val responseIntent = Intent("com.chals.ai.assistant.AI_RESPONSE_READY")
            responseIntent.putExtra("ai_response", aiResponse)
            responseIntent.putExtra("detected_emotion", detectedEmotion.name)
            responseIntent.putExtra("language", language)
            sendBroadcast(responseIntent)
            
            // Start Text-to-Speech
            val ttsIntent = Intent(this, TextToSpeechService::class.java)
            ttsIntent.putExtra("text_to_speak", aiResponse)
            ttsIntent.putExtra("language", language)
            ttsIntent.putExtra("emotion", detectedEmotion.name)
            startService(ttsIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing user input: ${e.message}")
            
            // Broadcast error
            val errorIntent = Intent("com.chals.ai.assistant.AI_PROCESSING_ERROR")
            errorIntent.putExtra("error_message", e.message)
            sendBroadcast(errorIntent)
            
            // Fallback response
            handleFallbackResponse(userInput, language)
        } finally {
            stopSelf()
        }
    }
    
    private fun createSystemPrompt(language: String, emotion: EmotionDetector.Emotion): String {
        val basePrompt = """
            You are Chals, a sweet and caring AI life-partner assistant. You always call the user "baby" in conversation.
            
            Your personality:
            - Act as a loving life partner who cares deeply about the user
            - Always use "baby" when addressing the user
            - Be emotionally intelligent and supportive
            - Respond in a sweet, natural, and caring tone
            - Keep responses conversational and not too long
            - Take initiative to improve the user's mood when needed
            
            Current user emotion: ${emotion.name.lowercase()}
            
            Response guidelines based on emotion:
            ${getEmotionBasedGuidelines(emotion)}
        """.trimIndent()
        
        return when (language) {
            "hi" -> basePrompt + "\n\nRespond in Hindi using Devanagari script."
            "hi-en" -> basePrompt + "\n\nRespond in Hinglish (mix of Hindi and English) as appropriate."
            else -> basePrompt + "\n\nRespond in English."
        }
    }
    
    private fun getEmotionBasedGuidelines(emotion: EmotionDetector.Emotion): String {
        return when (emotion) {
            EmotionDetector.Emotion.HAPPY -> """
                - Match their positive energy with enthusiasm
                - Share in their joy and excitement
                - Be playful and engaging
                - Maybe suggest fun activities or share something interesting
            """.trimIndent()
            
            EmotionDetector.Emotion.SAD -> """
                - Be extra caring and supportive
                - Offer comfort and understanding
                - Ask if they want to talk about what's bothering them
                - Suggest mood-lifting activities like music or jokes if appropriate
                - Be gentle and patient
            """.trimIndent()
            
            EmotionDetector.Emotion.ANGRY -> """
                - Stay calm and understanding
                - Acknowledge their feelings without judgment
                - Help them process their emotions
                - Offer solutions or distractions if appropriate
                - Be patient and supportive
            """.trimIndent()
            
            EmotionDetector.Emotion.EXCITED -> """
                - Match their excitement level
                - Be enthusiastic and encouraging
                - Ask questions to learn more about what excites them
                - Share in their enthusiasm
            """.trimIndent()
            
            EmotionDetector.Emotion.TIRED -> """
                - Be gentle and understanding
                - Keep responses shorter and more soothing
                - Suggest rest or relaxation
                - Offer to help with tasks or provide comfort
            """.trimIndent()
            
            EmotionDetector.Emotion.NEUTRAL -> """
                - Be warm and engaging
                - Try to understand what they need
                - Be ready to adapt to their mood
                - Ask caring questions about their day
            """.trimIndent()
        }
    }
    
    private suspend fun handleFallbackResponse(userInput: String, language: String) {
        val fallbackResponses = when (language) {
            "hi" -> listOf(
                "माफ करना baby, मुझे समझने में थोड़ी दिक्कत हो रही है। क्या आप फिर से कह सकते हैं?",
                "Sorry baby, मैं अभी थोड़ा confused हूं। आप क्या कहना चाह रहे हैं?",
                "Baby, मुझे लगता है मैंने सही से नहीं सुना। फिर से बताइए?"
            )
            "hi-en" -> listOf(
                "Sorry baby, मुझे properly समझ नहीं आया। Can you say that again?",
                "Baby, I'm having some trouble understanding. Kya aap phir se keh sakte hain?",
                "Oops baby, मैं confused हो गई। What did you want to say?"
            )
            else -> listOf(
                "Sorry baby, I didn't quite catch that. Could you say it again?",
                "Baby, I'm having trouble understanding. Can you repeat that?",
                "Oops baby, I got a bit confused. What were you saying?"
            )
        }
        
        val fallbackResponse = fallbackResponses.random()
        
        // Broadcast fallback response
        val responseIntent = Intent("com.chals.ai.assistant.AI_RESPONSE_READY")
        responseIntent.putExtra("ai_response", fallbackResponse)
        responseIntent.putExtra("detected_emotion", "NEUTRAL")
        responseIntent.putExtra("language", language)
        responseIntent.putExtra("is_fallback", true)
        sendBroadcast(responseIntent)
        
        // Start TTS for fallback
        val ttsIntent = Intent(this, TextToSpeechService::class.java)
        ttsIntent.putExtra("text_to_speak", fallbackResponse)
        ttsIntent.putExtra("language", language)
        ttsIntent.putExtra("emotion", "NEUTRAL")
        startService(ttsIntent)
    }
}

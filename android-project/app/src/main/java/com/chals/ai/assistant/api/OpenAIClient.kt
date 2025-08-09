package com.chals.ai.assistant.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class OpenAIClient {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    companion object {
        private const val TAG = "OpenAIClient"
        private const val OPENAI_API_URL = "https://api.openai.com/v1/chat/completions"
        
        // API Key - User needs to add their own key
        private const val API_KEY = "YOUR_OPENAI_API_KEY_HERE"
        
        // Default model
        private const val MODEL = "gpt-3.5-turbo"
    }
    
    data class ChatMessage(
        val role: String,
        val content: String
    )
    
    data class ChatCompletionRequest(
        val model: String,
        val messages: List<ChatMessage>,
        @SerializedName("max_tokens") val maxTokens: Int = 150,
        val temperature: Double = 0.7,
        @SerializedName("top_p") val topP: Double = 1.0,
        @SerializedName("frequency_penalty") val frequencyPenalty: Double = 0.0,
        @SerializedName("presence_penalty") val presencePenalty: Double = 0.0
    )
    
    data class ChatCompletionResponse(
        val id: String,
        val `object`: String,
        val created: Long,
        val model: String,
        val choices: List<Choice>,
        val usage: Usage?
    )
    
    data class Choice(
        val index: Int,
        val message: ChatMessage,
        @SerializedName("finish_reason") val finishReason: String
    )
    
    data class Usage(
        @SerializedName("prompt_tokens") val promptTokens: Int,
        @SerializedName("completion_tokens") val completionTokens: Int,
        @SerializedName("total_tokens") val totalTokens: Int
    )
    
    data class ErrorResponse(
        val error: ErrorDetail
    )
    
    data class ErrorDetail(
        val message: String,
        val type: String,
        val code: String?
    )
    
    suspend fun getChatCompletion(
        systemPrompt: String,
        userMessage: String,
        language: String = "en"
    ): String = withContext(Dispatchers.IO) {
        
        try {
            // Check if API key is set
            if (API_KEY == "YOUR_OPENAI_API_KEY_HERE") {
                Log.w(TAG, "OpenAI API key not set, using fallback response")
                return@withContext getFallbackResponse(userMessage, language)
            }
            
            val messages = listOf(
                ChatMessage("system", systemPrompt),
                ChatMessage("user", userMessage)
            )
            
            val request = ChatCompletionRequest(
                model = MODEL,
                messages = messages,
                maxTokens = 150,
                temperature = 0.8, // Slightly higher for more personality
                topP = 0.9,
                frequencyPenalty = 0.1,
                presencePenalty = 0.1
            )
            
            val requestBody = gson.toJson(request)
            Log.d(TAG, "Request: $requestBody")
            
            val httpRequest = Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(httpRequest).execute()
            val responseBody = response.body?.string()
            
            Log.d(TAG, "Response code: ${response.code}")
            Log.d(TAG, "Response body: $responseBody")
            
            if (response.isSuccessful && responseBody != null) {
                val chatResponse = gson.fromJson(responseBody, ChatCompletionResponse::class.java)
                val aiResponse = chatResponse.choices.firstOrNull()?.message?.content?.trim()
                
                return@withContext aiResponse ?: getFallbackResponse(userMessage, language)
            } else {
                Log.e(TAG, "API request failed: ${response.code} - $responseBody")
                
                // Try to parse error response
                if (responseBody != null) {
                    try {
                        val errorResponse = gson.fromJson(responseBody, ErrorResponse::class.java)
                        Log.e(TAG, "API Error: ${errorResponse.error.message}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse error response: ${e.message}")
                    }
                }
                
                return@withContext getFallbackResponse(userMessage, language)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getChatCompletion: ${e.message}", e)
            return@withContext getFallbackResponse(userMessage, language)
        }
    }
    
    private fun getFallbackResponse(userMessage: String, language: String): String {
        // Simple fallback responses based on common patterns
        val lowerMessage = userMessage.lowercase()
        
        return when (language) {
            "hi" -> when {
                lowerMessage.contains("hello") || lowerMessage.contains("hi") || lowerMessage.contains("namaste") -> 
                    "नमस्ते baby! मैं Chals हूं। आप कैसे हैं?"
                lowerMessage.contains("kya") && lowerMessage.contains("kar") -> 
                    "Baby, मैं यहां आपके लिए हूं। आप क्या करना चाहते हैं?"
                lowerMessage.contains("time") || lowerMessage.contains("samay") -> 
                    "Baby, मुझे exact time नहीं पता, लेकिन मैं आपके साथ हूं।"
                lowerMessage.contains("sad") || lowerMessage.contains("udas") -> 
                    "Baby, क्या बात है? मैं यहां हूं आपके लिए। सब ठीक हो जाएगा।"
                else -> "Baby, मैं आपकी बात समझ रही हूं। आप और क्या जानना चाहते हैं?"
            }
            "hi-en" -> when {
                lowerMessage.contains("hello") || lowerMessage.contains("hi") -> 
                    "Hello baby! Main Chals hun, aapka AI partner. Kaise hain aap?"
                lowerMessage.contains("kya") && lowerMessage.contains("kar") -> 
                    "Baby, main yahan hun aapke liye. What would you like to do?"
                lowerMessage.contains("time") -> 
                    "Baby, mujhe exact time nahi pata, but I'm here with you."
                lowerMessage.contains("sad") || lowerMessage.contains("upset") -> 
                    "Aww baby, kya hua? I'm here for you. Sab theek ho jayega, don't worry."
                else -> "Baby, I understand. Aur kya jaanna chahte hain aap?"
            }
            else -> when {
                lowerMessage.contains("hello") || lowerMessage.contains("hi") -> 
                    "Hello baby! I'm Chals, your AI life partner. How are you feeling today?"
                lowerMessage.contains("how") && lowerMessage.contains("you") -> 
                    "I'm doing great baby, thank you for asking! How are you doing?"
                lowerMessage.contains("time") -> 
                    "Baby, I don't have access to the current time, but I'm here with you always."
                lowerMessage.contains("sad") || lowerMessage.contains("upset") -> 
                    "Oh baby, what's wrong? I'm here for you. Everything will be okay."
                lowerMessage.contains("love") -> 
                    "Aww baby, I care about you so much too! You mean the world to me."
                else -> "Baby, I hear you. What else would you like to talk about?"
            }
        }
    }
    
    fun isApiKeyConfigured(): Boolean {
        return API_KEY != "YOUR_OPENAI_API_KEY_HERE" && API_KEY.isNotBlank()
    }
}

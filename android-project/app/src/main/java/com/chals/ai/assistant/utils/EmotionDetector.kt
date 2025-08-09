package com.chals.ai.assistant.utils

import android.util.Log
import java.util.regex.Pattern

class EmotionDetector {
    
    companion object {
        private const val TAG = "EmotionDetector"
    }
    
    enum class Emotion {
        HAPPY,
        SAD,
        ANGRY,
        EXCITED,
        TIRED,
        NEUTRAL
    }
    
    // Emotion keywords for different languages
    private val happyKeywords = mapOf(
        "en" to setOf(
            "happy", "joy", "joyful", "excited", "great", "awesome", "amazing", "wonderful",
            "fantastic", "excellent", "good", "nice", "love", "loving", "cheerful", "glad",
            "delighted", "thrilled", "elated", "ecstatic", "blissful", "content", "pleased",
            "yay", "woohoo", "haha", "lol", "smile", "smiling", "laugh", "laughing"
        ),
        "hi" to setOf(
            "khush", "khushi", "prasann", "anand", "maza", "accha", "badhiya", "zabardast",
            "kamaal", "shandar", "sundar", "pyaar", "mohabbat", "hasna", "muskurana",
            "khushiyan", "umang", "josh", "utsah"
        ),
        "hinglish" to setOf(
            "khush", "happy", "maza", "fun", "accha", "good", "badhiya", "great",
            "zabardast", "awesome", "kamaal", "amazing", "mast", "cool", "bindaas"
        )
    )
    
    private val sadKeywords = mapOf(
        "en" to setOf(
            "sad", "sadness", "unhappy", "depressed", "down", "low", "blue", "upset",
            "disappointed", "heartbroken", "miserable", "gloomy", "melancholy", "sorrowful",
            "grief", "crying", "cry", "tears", "hurt", "pain", "lonely", "alone",
            "devastated", "crushed", "broken", "hopeless", "despair"
        ),
        "hi" to setOf(
            "udas", "dukhi", "pareshan", "tension", "chinta", "gam", "dard", "takleef",
            "rona", "aansu", "akela", "nirash", "hatash", "dukhad", "vyakulta"
        ),
        "hinglish" to setOf(
            "udas", "sad", "dukhi", "upset", "pareshan", "worried", "tension", "stress",
            "down", "low", "hurt", "pain", "rona", "cry", "akela", "alone"
        )
    )
    
    private val angryKeywords = mapOf(
        "en" to setOf(
            "angry", "mad", "furious", "rage", "annoyed", "irritated", "frustrated",
            "pissed", "livid", "outraged", "enraged", "irate", "fuming", "heated",
            "hate", "disgusted", "fed up", "sick of", "damn", "hell", "stupid", "idiot"
        ),
        "hi" to setOf(
            "gussa", "krodh", "naraz", "chidh", "pareshan", "ghussa", "khafa",
            "badtameez", "pagal", "bewakoof", "nautanki", "bakwas"
        ),
        "hinglish" to setOf(
            "gussa", "angry", "mad", "naraz", "upset", "frustrated", "irritated",
            "pagal", "stupid", "bakwas", "nonsense", "fed up", "sick"
        )
    )
    
    private val excitedKeywords = mapOf(
        "en" to setOf(
            "excited", "thrilled", "pumped", "energetic", "enthusiastic", "eager",
            "can't wait", "amazing", "incredible", "unbelievable", "wow", "omg",
            "fantastic", "brilliant", "outstanding", "superb", "marvelous"
        ),
        "hi" to setOf(
            "utsahit", "josh", "umang", "jazbaat", "excitement", "energy", "shakti",
            "hausla", "himmat", "junoon", "deewana", "pagal"
        ),
        "hinglish" to setOf(
            "excited", "josh", "energy", "pumped", "thrilled", "amazing", "incredible",
            "wow", "omg", "fantastic", "brilliant", "zabardast", "kamaal"
        )
    )
    
    private val tiredKeywords = mapOf(
        "en" to setOf(
            "tired", "exhausted", "sleepy", "drowsy", "weary", "fatigued", "drained",
            "worn out", "beat", "spent", "lazy", "lethargic", "sluggish", "yawn",
            "sleep", "rest", "nap", "bed", "can't keep eyes open"
        ),
        "hi" to setOf(
            "thak", "thaka", "neend", "sona", "aaram", "vishram", "kamzor",
            "shithil", "alsi", "sust", "jhamak", "angdai"
        ),
        "hinglish" to setOf(
            "tired", "thaka", "sleepy", "neend", "drowsy", "exhausted", "drained",
            "lazy", "alsi", "rest", "aaram", "sleep", "sona"
        )
    )
    
    fun detectEmotionFromText(text: String): Emotion {
        if (text.isBlank()) return Emotion.NEUTRAL
        
        val cleanText = text.lowercase().trim()
        Log.d(TAG, "Analyzing emotion for text: $cleanText")
        
        // Count emotion indicators
        val emotionScores = mutableMapOf<Emotion, Int>()
        
        // Check for different language patterns
        val languages = listOf("en", "hi", "hinglish")
        
        for (language in languages) {
            emotionScores[Emotion.HAPPY] = (emotionScores[Emotion.HAPPY] ?: 0) + 
                countMatches(cleanText, happyKeywords[language] ?: emptySet())
            
            emotionScores[Emotion.SAD] = (emotionScores[Emotion.SAD] ?: 0) + 
                countMatches(cleanText, sadKeywords[language] ?: emptySet())
            
            emotionScores[Emotion.ANGRY] = (emotionScores[Emotion.ANGRY] ?: 0) + 
                countMatches(cleanText, angryKeywords[language] ?: emptySet())
            
            emotionScores[Emotion.EXCITED] = (emotionScores[Emotion.EXCITED] ?: 0) + 
                countMatches(cleanText, excitedKeywords[language] ?: emptySet())
            
            emotionScores[Emotion.TIRED] = (emotionScores[Emotion.TIRED] ?: 0) + 
                countMatches(cleanText, tiredKeywords[language] ?: emptySet())
        }
        
        // Check for punctuation-based emotion indicators
        emotionScores[Emotion.EXCITED] = (emotionScores[Emotion.EXCITED] ?: 0) + 
            countExclamationMarks(text) * 2
        
        emotionScores[Emotion.HAPPY] = (emotionScores[Emotion.HAPPY] ?: 0) + 
            countEmoticons(text, listOf(":)", ":-)", ":D", "😊", "😄", "😃", "🙂", "😁"))
        
        emotionScores[Emotion.SAD] = (emotionScores[Emotion.SAD] ?: 0) + 
            countEmoticons(text, listOf(":(", ":-(", "😢", "😭", "😞", "😔", "☹️"))
        
        emotionScores[Emotion.ANGRY] = (emotionScores[Emotion.ANGRY] ?: 0) + 
            countEmoticons(text, listOf("😠", "😡", "🤬", "😤")) + 
            if (text.contains("!!!") || text.uppercase() == text) 1 else 0
        
        Log.d(TAG, "Emotion scores: $emotionScores")
        
        // Find the emotion with the highest score
        val maxEmotion = emotionScores.maxByOrNull { it.value }
        
        return if (maxEmotion != null && maxEmotion.value > 0) {
            Log.d(TAG, "Detected emotion: ${maxEmotion.key} (score: ${maxEmotion.value})")
            maxEmotion.key
        } else {
            Log.d(TAG, "No specific emotion detected, defaulting to NEUTRAL")
            Emotion.NEUTRAL
        }
    }
    
    private fun countMatches(text: String, keywords: Set<String>): Int {
        var count = 0
        for (keyword in keywords) {
            val pattern = "\\b${Pattern.quote(keyword)}\\b"
            if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text).find()) {
                count++
                Log.d(TAG, "Found emotion keyword: $keyword")
            }
        }
        return count
    }
    
    private fun countExclamationMarks(text: String): Int {
        return text.count { it == '!' }
    }
    
    private fun countEmoticons(text: String, emoticons: List<String>): Int {
        var count = 0
        for (emoticon in emoticons) {
            count += text.split(emoticon).size - 1
        }
        return count
    }
    
    fun getEmotionDescription(emotion: Emotion): String {
        return when (emotion) {
            Emotion.HAPPY -> "Happy and positive"
            Emotion.SAD -> "Sad or upset"
            Emotion.ANGRY -> "Angry or frustrated"
            Emotion.EXCITED -> "Excited and enthusiastic"
            Emotion.TIRED -> "Tired or sleepy"
            Emotion.NEUTRAL -> "Neutral or calm"
        }
    }
    
    fun getEmotionColor(emotion: Emotion): String {
        return when (emotion) {
            Emotion.HAPPY -> "#4CAF50" // Green
            Emotion.SAD -> "#2196F3" // Blue
            Emotion.ANGRY -> "#F44336" // Red
            Emotion.EXCITED -> "#FF9800" // Orange
            Emotion.TIRED -> "#9C27B0" // Purple
            Emotion.NEUTRAL -> "#607D8B" // Blue Grey
        }
    }
    
    // Method to detect emotion from audio characteristics (placeholder for future implementation)
    fun detectEmotionFromAudio(audioData: ByteArray): Emotion {
        // TODO: Implement audio-based emotion detection
        // This would analyze pitch, tone, speed, etc.
        // For now, return neutral
        Log.d(TAG, "Audio emotion detection not yet implemented")
        return Emotion.NEUTRAL
    }
}

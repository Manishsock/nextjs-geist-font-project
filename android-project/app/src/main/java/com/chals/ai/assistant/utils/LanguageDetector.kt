package com.chals.ai.assistant.utils

import android.util.Log
import java.util.regex.Pattern

class LanguageDetector {
    
    companion object {
        private const val TAG = "LanguageDetector"
        
        // Hindi Unicode ranges
        private val HINDI_PATTERN = Pattern.compile("[\\u0900-\\u097F]+")
        
        // Common Hindi words in Roman script (Hinglish)
        private val HINGLISH_WORDS = setOf(
            "kya", "hai", "hain", "kar", "karo", "kaise", "kaun", "kahan", "kab", "kyun",
            "acha", "accha", "theek", "thik", "bhi", "aur", "ya", "nahi", "nahin", "haan",
            "ji", "sahab", "madam", "bhai", "didi", "uncle", "aunty", "beta", "beta",
            "mummy", "papa", "dada", "nana", "nani", "dadi", "ghar", "paani", "khana",
            "suno", "dekho", "chalo", "aao", "jao", "ruko", "bas", "abhi", "phir",
            "kuch", "koi", "sab", "sabko", "mujhe", "tumhe", "usse", "iske", "uske",
            "mere", "tere", "uske", "hamara", "tumhara", "unka", "yahan", "wahan",
            "kal", "aaj", "parso", "subah", "sham", "raat", "din", "samay", "time"
        )
        
        // Common English words
        private val ENGLISH_WORDS = setOf(
            "the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with",
            "by", "from", "up", "about", "into", "through", "during", "before", "after",
            "above", "below", "between", "among", "this", "that", "these", "those",
            "i", "you", "he", "she", "it", "we", "they", "me", "him", "her", "us", "them",
            "my", "your", "his", "her", "its", "our", "their", "mine", "yours", "ours",
            "what", "when", "where", "why", "how", "who", "which", "whose", "whom",
            "is", "am", "are", "was", "were", "be", "been", "being", "have", "has", "had",
            "do", "does", "did", "will", "would", "could", "should", "may", "might", "must",
            "can", "cannot", "can't", "won't", "wouldn't", "couldn't", "shouldn't",
            "hello", "hi", "hey", "bye", "goodbye", "please", "thank", "thanks", "sorry"
        )
    }
    
    enum class Language {
        HINDI,
        ENGLISH,
        HINGLISH,
        UNKNOWN
    }
    
    fun detectLanguage(text: String): Language {
        if (text.isBlank()) return Language.UNKNOWN
        
        val cleanText = text.lowercase().trim()
        val words = cleanText.split("\\s+".toRegex())
        
        Log.d(TAG, "Analyzing text: $cleanText")
        Log.d(TAG, "Words: ${words.joinToString(", ")}")
        
        // Check for Devanagari script (Hindi)
        val hasDevanagari = HINDI_PATTERN.matcher(text).find()
        if (hasDevanagari) {
            Log.d(TAG, "Detected Devanagari script - Language: HINDI")
            return Language.HINDI
        }
        
        // Count language indicators
        var hinglishScore = 0
        var englishScore = 0
        
        for (word in words) {
            val cleanWord = word.replace("[^a-zA-Z]".toRegex(), "")
            if (cleanWord.length < 2) continue
            
            when {
                HINGLISH_WORDS.contains(cleanWord) -> {
                    hinglishScore += 2
                    Log.d(TAG, "Hinglish word found: $cleanWord")
                }
                ENGLISH_WORDS.contains(cleanWord) -> {
                    englishScore += 1
                    Log.d(TAG, "English word found: $cleanWord")
                }
                isLikelyHinglish(cleanWord) -> {
                    hinglishScore += 1
                    Log.d(TAG, "Likely Hinglish word: $cleanWord")
                }
            }
        }
        
        Log.d(TAG, "Scores - Hinglish: $hinglishScore, English: $englishScore")
        
        // Determine language based on scores
        val result = when {
            hinglishScore > englishScore && hinglishScore > 0 -> Language.HINGLISH
            englishScore > hinglishScore && englishScore > 0 -> Language.ENGLISH
            hinglishScore == englishScore && hinglishScore > 0 -> Language.HINGLISH // Default to Hinglish for mixed
            else -> {
                // Fallback: check for common patterns
                when {
                    containsHinglishPatterns(cleanText) -> Language.HINGLISH
                    containsEnglishPatterns(cleanText) -> Language.ENGLISH
                    else -> Language.ENGLISH // Default fallback
                }
            }
        }
        
        Log.d(TAG, "Final detected language: $result")
        return result
    }
    
    private fun isLikelyHinglish(word: String): Boolean {
        // Check for common Hinglish patterns
        return when {
            word.endsWith("ji") && word.length > 2 -> true
            word.endsWith("wala") || word.endsWith("wali") -> true
            word.contains("aa") || word.contains("ee") || word.contains("oo") -> true
            word.matches(".*[aeiou]{2,}.*".toRegex()) -> true // Multiple vowels
            else -> false
        }
    }
    
    private fun containsHinglishPatterns(text: String): Boolean {
        val hinglishPatterns = listOf(
            "\\bkya\\b", "\\bhai\\b", "\\bhain\\b", "\\bkar\\b", "\\bacha\\b",
            "\\btheek\\b", "\\bnahi\\b", "\\bhaan\\b", "\\bji\\b", "\\bbhi\\b"
        )
        
        return hinglishPatterns.any { pattern ->
            Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text).find()
        }
    }
    
    private fun containsEnglishPatterns(text: String): Boolean {
        val englishPatterns = listOf(
            "\\bthe\\b", "\\band\\b", "\\bwhat\\b", "\\bhow\\b", "\\bwhen\\b",
            "\\bwhere\\b", "\\bwhy\\b", "\\bcan\\b", "\\bwill\\b", "\\bis\\b"
        )
        
        return englishPatterns.any { pattern ->
            Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text).find()
        }
    }
    
    fun getLanguageCode(language: Language): String {
        return when (language) {
            Language.HINDI -> "hi"
            Language.ENGLISH -> "en"
            Language.HINGLISH -> "hi-en" // Mixed
            Language.UNKNOWN -> "en" // Default to English
        }
    }
    
    fun getLanguageName(language: Language): String {
        return when (language) {
            Language.HINDI -> "Hindi"
            Language.ENGLISH -> "English"
            Language.HINGLISH -> "Hinglish"
            Language.UNKNOWN -> "Unknown"
        }
    }
}

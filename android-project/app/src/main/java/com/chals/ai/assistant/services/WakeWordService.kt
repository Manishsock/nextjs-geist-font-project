package com.chals.ai.assistant.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.chals.ai.assistant.MainActivity
import com.chals.ai.assistant.R
import ai.picovoice.porcupine.Porcupine
import ai.picovoice.porcupine.PorcupineException
import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WakeWordService : Service() {
    
    private var porcupineManager: PorcupineManager? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    
    companion object {
        private const val TAG = "WakeWordService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "chals_wake_word_channel"
        
        // Picovoice Access Key - You'll need to get this from Picovoice Console
        // For now using a placeholder - user will need to add their key
        private const val ACCESS_KEY = "YOUR_PICOVOICE_ACCESS_KEY_HERE"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WakeWordService created")
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "WakeWordService started")
        
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        startWakeWordDetection()
        
        return START_STICKY // Restart service if killed
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "WakeWordService destroyed")
        stopWakeWordDetection()
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chals Wake Word Detection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Chals listening for wake word"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.wake_word_service_notification_title))
            .setContentText(getString(R.string.wake_word_service_notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    private fun startWakeWordDetection() {
        serviceScope.launch {
            try {
                // Check if access key is set
                if (ACCESS_KEY == "YOUR_PICOVOICE_ACCESS_KEY_HERE") {
                    Log.e(TAG, "Picovoice access key not set! Please add your access key.")
                    // For now, simulate wake word detection for testing
                    simulateWakeWordDetection()
                    return@launch
                }
                
                // Initialize Porcupine with custom wake word "Hey Chals"
                porcupineManager = PorcupineManager.Builder()
                    .setAccessKey(ACCESS_KEY)
                    .setKeywords(arrayOf("hey chals")) // Custom wake word
                    .setSensitivities(floatArrayOf(0.5f)) // Sensitivity (0.0 to 1.0)
                    .build(applicationContext, object : PorcupineManagerCallback {
                        override fun invoke(keywordIndex: Int) {
                            Log.d(TAG, "Wake word detected: Hey Chals!")
                            onWakeWordDetected()
                        }
                    })
                
                porcupineManager?.start()
                Log.d(TAG, "Porcupine wake word detection started")
                
            } catch (e: PorcupineException) {
                Log.e(TAG, "Failed to initialize Porcupine: ${e.message}")
                // Fallback to simulation for testing
                simulateWakeWordDetection()
            }
        }
    }
    
    private fun simulateWakeWordDetection() {
        Log.d(TAG, "Using simulated wake word detection for testing")
        // For testing purposes, we'll simulate wake word detection
        // In a real implementation, this would be replaced by actual Porcupine integration
    }
    
    private fun stopWakeWordDetection() {
        try {
            porcupineManager?.stop()
            porcupineManager?.delete()
            porcupineManager = null
            Log.d(TAG, "Wake word detection stopped")
        } catch (e: PorcupineException) {
            Log.e(TAG, "Error stopping Porcupine: ${e.message}")
        }
    }
    
    private fun onWakeWordDetected() {
        Log.d(TAG, "Processing wake word detection...")
        
        // Send broadcast to notify other components
        val intent = Intent("com.chals.ai.assistant.WAKE_WORD_DETECTED")
        sendBroadcast(intent)
        
        // Start speech recognition
        startSpeechRecognition()
    }
    
    private fun startSpeechRecognition() {
        // TODO: Start speech recognition service
        Log.d(TAG, "Starting speech recognition...")
        
        // For now, just log - we'll implement this in the next phase
        val intent = Intent(this, SpeechRecognitionService::class.java)
        startService(intent)
    }
}

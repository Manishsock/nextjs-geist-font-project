package com.chals.ai.assistant.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.chals.ai.assistant.services.WakeWordService

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Boot receiver triggered: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d(TAG, "Device booted or app updated, starting Chals services")
                
                // Check if user has previously enabled Chals
                val sharedPrefs = context.getSharedPreferences("chals_prefs", Context.MODE_PRIVATE)
                val isChalsEnabled = sharedPrefs.getBoolean("chals_enabled", false)
                
                if (isChalsEnabled) {
                    // Start wake word service
                    val wakeWordIntent = Intent(context, WakeWordService::class.java)
                    context.startForegroundService(wakeWordIntent)
                    
                    Log.d(TAG, "Chals wake word service started on boot")
                } else {
                    Log.d(TAG, "Chals is disabled, not starting services")
                }
            }
        }
    }
}

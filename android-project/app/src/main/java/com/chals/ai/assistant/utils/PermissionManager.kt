package com.chals.ai.assistant.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {
    
    companion object {
        const val MICROPHONE_PERMISSION_CODE = 100
        const val PHONE_PERMISSION_CODE = 101
        const val SMS_PERMISSION_CODE = 102
        const val STORAGE_PERMISSION_CODE = 103
        const val OVERLAY_PERMISSION_CODE = 104
    }
    
    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasPhonePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasSmsPermissions(): Boolean {
        val readSms = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        
        val sendSms = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
        
        return readSms && sendSms
    }
    
    fun hasStoragePermissions(): Boolean {
        val readStorage = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        
        val writeStorage = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        
        return readStorage && writeStorage
    }
    
    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }
    
    fun requestRecordAudioPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            MICROPHONE_PERMISSION_CODE
        )
    }
    
    fun requestPhonePermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS
            ),
            PHONE_PERMISSION_CODE
        )
    }
    
    fun requestSmsPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS
            ),
            SMS_PERMISSION_CODE
        )
    }
    
    fun requestStoragePermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            STORAGE_PERMISSION_CODE
        )
    }
    
    fun getAllRequiredPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    
    fun checkAllPermissions(): Boolean {
        return getAllRequiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}

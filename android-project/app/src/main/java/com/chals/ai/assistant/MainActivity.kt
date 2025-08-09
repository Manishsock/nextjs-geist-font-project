package com.chals.ai.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chals.ai.assistant.databinding.ActivityMainBinding
import com.chals.ai.assistant.services.WakeWordService
import com.chals.ai.assistant.services.FloatingBubbleService
import com.chals.ai.assistant.utils.PermissionManager

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionManager: PermissionManager
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1002
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        permissionManager = PermissionManager(this)
        
        setupUI()
        checkPermissions()
    }
    
    private fun setupUI() {
        // Set initial status
        binding.statusTextView.text = getString(R.string.hello_baby)
        binding.wakeWordStatusTextView.text = "Wake word: Inactive"
        
        // Settings button click
        binding.settingsButton.setOnClickListener {
            // TODO: Open settings activity
            Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        // Floating bubble button click
        binding.floatingBubbleButton.setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                toggleFloatingBubble()
            } else {
                requestOverlayPermission()
            }
        }
        
        // Avatar click for manual activation
        binding.avatarContainer.setOnClickListener {
            // TODO: Manually activate Chals for testing
            Toast.makeText(this, "Manual activation coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun checkPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.WAKE_LOCK
        )
        
        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            startWakeWordService()
        }
    }
    
    private fun startWakeWordService() {
        val intent = Intent(this, WakeWordService::class.java)
        startForegroundService(intent)
        binding.wakeWordStatusTextView.text = "Wake word: Active - Say 'Hey Chals'"
        
        // Update status
        binding.statusTextView.text = "I'm ready baby! Say 'Hey Chals' to talk to me."
    }
    
    private fun toggleFloatingBubble() {
        val intent = Intent(this, FloatingBubbleService::class.java)
        
        if (isFloatingBubbleActive()) {
            stopService(intent)
            binding.floatingBubbleButton.text = "Enable Bubble"
        } else {
            startService(intent)
            binding.floatingBubbleButton.text = "Disable Bubble"
        }
    }
    
    private fun isFloatingBubbleActive(): Boolean {
        // TODO: Implement proper check for floating bubble service
        return false
    }
    
    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    startWakeWordService()
                } else {
                    Toast.makeText(
                        this,
                        "Permissions are required for Chals to work properly",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            OVERLAY_PERMISSION_REQUEST_CODE -> {
                if (Settings.canDrawOverlays(this)) {
                    toggleFloatingBubble()
                } else {
                    Toast.makeText(
                        this,
                        "Overlay permission is required for floating bubble",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Update UI state when returning to app
        updateUIState()
    }
    
    private fun updateUIState() {
        // TODO: Check if services are running and update UI accordingly
        binding.statusTextView.text = "Welcome back baby! I'm here for you."
    }
}

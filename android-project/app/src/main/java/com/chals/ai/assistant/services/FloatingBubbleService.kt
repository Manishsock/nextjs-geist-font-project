package com.chals.ai.assistant.services

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.chals.ai.assistant.R

class FloatingBubbleService : Service() {
    
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var isFloatingViewVisible = false
    
    companion object {
        private const val TAG = "FloatingBubbleService"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "FloatingBubbleService created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isFloatingViewVisible) {
            showFloatingBubble()
        }
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "FloatingBubbleService destroyed")
        hideFloatingBubble()
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    private fun showFloatingBubble() {
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            
            // Inflate the floating bubble layout
            floatingView = LayoutInflater.from(this).inflate(R.layout.floating_bubble_layout, null)
            
            // Set up window parameters
            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            
            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 100
                y = 100
            }
            
            // Add the view to window manager
            windowManager?.addView(floatingView, params)
            isFloatingViewVisible = true
            
            // Set up touch listener for dragging and clicking
            setupTouchListener()
            
            Log.d(TAG, "Floating bubble shown")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing floating bubble: ${e.message}")
            Toast.makeText(this, "Error showing floating bubble", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun hideFloatingBubble() {
        try {
            if (floatingView != null && windowManager != null) {
                windowManager?.removeView(floatingView)
                floatingView = null
                isFloatingViewVisible = false
                Log.d(TAG, "Floating bubble hidden")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding floating bubble: ${e.message}")
        }
    }
    
    private fun setupTouchListener() {
        val bubbleImageView = floatingView?.findViewById<ImageView>(R.id.bubbleImageView)
        
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        
        bubbleImageView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params?.x ?: 0
                    initialY = params?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    
                    // Check if user is dragging (moved more than a threshold)
                    if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                        isDragging = true
                        
                        params?.x = initialX + deltaX.toInt()
                        params?.y = initialY + deltaY.toInt()
                        
                        windowManager?.updateViewLayout(floatingView, params)
                    }
                    true
                }
                
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // This was a click, not a drag
                        onBubbleClicked()
                    }
                    true
                }
                
                else -> false
            }
        }
    }
    
    private fun onBubbleClicked() {
        Log.d(TAG, "Floating bubble clicked")
        
        // Animate bubble (simple scale animation)
        animateBubbleClick()
        
        // Trigger wake word detection manually
        val intent = Intent("com.chals.ai.assistant.WAKE_WORD_DETECTED")
        sendBroadcast(intent)
        
        // Start speech recognition
        val speechIntent = Intent(this, SpeechRecognitionService::class.java)
        startService(speechIntent)
        
        // Show feedback to user
        Toast.makeText(this, "Listening... Speak now!", Toast.LENGTH_SHORT).show()
    }
    
    private fun animateBubbleClick() {
        val bubbleImageView = floatingView?.findViewById<ImageView>(R.id.bubbleImageView)
        
        bubbleImageView?.animate()
            ?.scaleX(1.2f)
            ?.scaleY(1.2f)
            ?.setDuration(100)
            ?.withEndAction {
                bubbleImageView.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
            ?.start()
    }
    
    // Method to update bubble appearance based on Chals' state
    fun updateBubbleState(state: BubbleState) {
        val bubbleImageView = floatingView?.findViewById<ImageView>(R.id.bubbleImageView)
        
        when (state) {
            BubbleState.IDLE -> {
                bubbleImageView?.setImageResource(R.drawable.chals_bubble_idle)
                bubbleImageView?.clearAnimation()
            }
            BubbleState.LISTENING -> {
                bubbleImageView?.setImageResource(R.drawable.chals_bubble_listening)
                startPulseAnimation(bubbleImageView)
            }
            BubbleState.THINKING -> {
                bubbleImageView?.setImageResource(R.drawable.chals_bubble_thinking)
                startRotateAnimation(bubbleImageView)
            }
            BubbleState.SPEAKING -> {
                bubbleImageView?.setImageResource(R.drawable.chals_bubble_speaking)
                startBounceAnimation(bubbleImageView)
            }
        }
    }
    
    private fun startPulseAnimation(view: ImageView?) {
        view?.animate()
            ?.scaleX(1.1f)
            ?.scaleY(1.1f)
            ?.setDuration(500)
            ?.withEndAction {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(500)
                    .withEndAction {
                        if (isFloatingViewVisible) {
                            startPulseAnimation(view)
                        }
                    }
                    .start()
            }
            ?.start()
    }
    
    private fun startRotateAnimation(view: ImageView?) {
        view?.animate()
            ?.rotation(360f)
            ?.setDuration(1000)
            ?.withEndAction {
                if (isFloatingViewVisible) {
                    view.rotation = 0f
                    startRotateAnimation(view)
                }
            }
            ?.start()
    }
    
    private fun startBounceAnimation(view: ImageView?) {
        view?.animate()
            ?.translationY(-20f)
            ?.setDuration(300)
            ?.withEndAction {
                view.animate()
                    .translationY(0f)
                    .setDuration(300)
                    .withEndAction {
                        if (isFloatingViewVisible) {
                            startBounceAnimation(view)
                        }
                    }
                    .start()
            }
            ?.start()
    }
    
    enum class BubbleState {
        IDLE,
        LISTENING,
        THINKING,
        SPEAKING
    }
}

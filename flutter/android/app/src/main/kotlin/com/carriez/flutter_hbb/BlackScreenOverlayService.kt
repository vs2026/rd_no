package com.carriez.flutter_hbb

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class BlackScreenOverlayService : Service() {
    
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: FrameLayout
    private lateinit var layoutParams: WindowManager.LayoutParams
    private var isOverlayVisible = false
    
    companion object {
        private const val TAG = "BlackScreenOverlay"
        const val ACTION_SHOW_OVERLAY = "show_overlay"
        const val ACTION_HIDE_OVERLAY = "hide_overlay"
        const val EXTRA_MESSAGE = "message"
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createOverlayView()
    }
    
    @SuppressLint("ClickableViewAccessibility")
    private fun createOverlayView() {
        overlayView = FrameLayout(this)
        overlayView.setBackgroundColor(Color.BLACK)
        
        // Create text view for the message
        val textView = TextView(this).apply {
            text = "设备正在被远程控制中..."
            setTextColor(Color.WHITE)
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
        }
        
        overlayView.addView(textView)
        
        // Set up layout parameters for full screen overlay
        layoutParams = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_OVERLAY -> {
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "设备正在被远程控制中..."
                showOverlay(message)
            }
            ACTION_HIDE_OVERLAY -> {
                hideOverlay()
            }
        }
        return START_STICKY
    }
    
    private fun showOverlay(message: String) {
        if (!isOverlayVisible) {
            try {
                // Update message if text view exists
                val textView = overlayView.getChildAt(0) as? TextView
                textView?.text = message
                
                windowManager.addView(overlayView, layoutParams)
                isOverlayVisible = true
                Log.d(TAG, "Black screen overlay shown with message: $message")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show overlay: ${e.message}")
            }
        }
    }
    
    private fun hideOverlay() {
        if (isOverlayVisible) {
            try {
                windowManager.removeView(overlayView)
                isOverlayVisible = false
                Log.d(TAG, "Black screen overlay hidden")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to hide overlay: ${e.message}")
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isOverlayVisible) {
            hideOverlay()
        }
    }
}

package com.carriez.flutter_hbb

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView

class BlackoutOverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    companion object {
        @Volatile
        var isRunning: Boolean = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addOverlay()
        isRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        isRunning = false
    }

    private fun addOverlay() {
        if (overlayView != null) return

        val container = FrameLayout(this)
        container.setBackgroundColor(Color.BLACK)

        val tv = TextView(this)
        tv.text = "Screen hidden on device. Remote can still see."
        tv.setTextColor(Color.WHITE)
        tv.textSize = 16f
        tv.gravity = android.view.Gravity.CENTER

        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        lp.gravity = Gravity.CENTER
        container.addView(tv, lp)

        val flags = (WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            flags,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START

        windowManager.addView(container, params)
        overlayView = container
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }
}



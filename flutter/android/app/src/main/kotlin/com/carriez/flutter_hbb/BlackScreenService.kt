package com.carriez.flutter_hbb

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.view.ViewGroup

/**
 * 黑屏悬浮窗服务
 * 显示黑屏并覆盖整个屏幕，显示提示文字
 */
class BlackScreenService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var blackScreenView: ViewGroup
    private var isShowing = false

    companion object {
        private const val logTag = "BlackScreenService"
        const val ACTION_SHOW = "com.carriez.flutter_hbb.BLACK_SCREEN_SHOW"
        const val ACTION_HIDE = "com.carriez.flutter_hbb.BLACK_SCREEN_HIDE"
        const val ACTION_TOGGLE = "com.carriez.flutter_hbb.BLACK_SCREEN_TOGGLE"
        
        var instance: BlackScreenService? = null
        
        fun show() {
            instance?.showBlackScreen()
        }
        
        fun hide() {
            instance?.hideBlackScreen()
        }
        
        fun toggle() {
            instance?.toggleBlackScreen()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createBlackScreenView()
        Log.d(logTag, "BlackScreenService created")
    }

    override fun onDestroy() {
        super.onDestroy()
        hideBlackScreen()
        instance = null
        Log.d(logTag, "BlackScreenService destroyed")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createBlackScreenView() {
        // 创建一个包含TextView的容器
        blackScreenView = object : ViewGroup(this) {
            override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
                // 将TextView居中显示
                val child = getChildAt(0)
                val width = child.measuredWidth
                val height = child.measuredHeight
                val left = (r - l - width) / 2
                val top = (b - t - height) / 2
                child.layout(left, top, left + width, top + height)
            }
        }
        
        blackScreenView.setBackgroundColor(Color.BLACK)
        
        // 创建提示文字
        val textView = TextView(this).apply {
            text = "屏幕已锁定\n控制端仍可正常操作"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
        }
        
        blackScreenView.addView(textView)
        
        // 点击黑屏可关闭（可选）
        blackScreenView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // 可以选择是否允许点击关闭
                // hideBlackScreen()
            }
            true
        }
    }

    fun showBlackScreen() {
        if (isShowing) {
            Log.d(logTag, "Black screen already showing")
            return
        }
        
        try {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
                else 
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
            
            params.gravity = Gravity.TOP or Gravity.START
            params.x = 0
            params.y = 0
            
            windowManager.addView(blackScreenView, params)
            isShowing = true
            Log.d(logTag, "Black screen shown")
        } catch (e: Exception) {
            Log.e(logTag, "Failed to show black screen", e)
        }
    }

    fun hideBlackScreen() {
        if (!isShowing) {
            Log.d(logTag, "Black screen not showing")
            return
        }
        
        try {
            windowManager.removeView(blackScreenView)
            isShowing = false
            Log.d(logTag, "Black screen hidden")
        } catch (e: Exception) {
            Log.e(logTag, "Failed to hide black screen", e)
        }
    }
    
    fun toggleBlackScreen() {
        if (isShowing) {
            hideBlackScreen()
        } else {
            showBlackScreen()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> showBlackScreen()
            ACTION_HIDE -> hideBlackScreen()
            ACTION_TOGGLE -> toggleBlackScreen()
        }
        return START_STICKY
    }
}


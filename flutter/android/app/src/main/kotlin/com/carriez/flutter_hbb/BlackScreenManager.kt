package com.carriez.flutter_hbb

import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView

object BlackScreenManager {
    private var overlayView: FrameLayout? = null
    private var isShown = false

    fun toggle(context: Context) {
        if (isShown) hide(context) else show(context)
    }

    fun show(context: Context) {
        if (isShown) return
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            -3
        )
        layoutParams.gravity = Gravity.CENTER

        val frame = FrameLayout(context)
        frame.setBackgroundColor(0xFF000000.toInt()) // 黑色背景

        val text = TextView(context)
        text.text = "远程控制中"
        text.setTextColor(0xFFFFFFFF.toInt())
        text.textSize = 20f
        text.gravity = Gravity.CENTER

        frame.addView(text, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        wm.addView(frame, layoutParams)
        overlayView = frame
        isShown = true
    }

    fun hide(context: Context) {
        if (!isShown || overlayView == null) return
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.removeView(overlayView)
        overlayView = null
        isShown = false
    }
}

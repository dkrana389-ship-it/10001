package com.keepfocus.app.ui

import android.app.Activity
import android.os.Bundle
import android.os.CountDownTimer
import android.content.Intent
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.os.Build

class MotivationActivity : Activity() {
    private var timer: CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(0.82f)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.decorView.setBackgroundColor(Color.TRANSPARENT)
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(28,36,28,36); setBackgroundColor(Color.WHITE) }
        val title = TextView(this).apply { text="Pause. Your dream is waiting."; textSize=25f; setTextColor(Color.rgb(30,64,175)); gravity=Gravity.CENTER }
        val dream = TextView(this).apply { text=intent.getStringExtra("dream_line").orEmpty(); textSize=21f; setTextColor(Color.BLACK); gravity=Gravity.CENTER; setPadding(0,24,0,16) }
        val hey = TextView(this).apply { text="Hey ${intent.getStringExtra("nickname").orEmpty()}, remember why you started."; textSize=15f; setTextColor(Color.DKGRAY); gravity=Gravity.CENTER }
        val count = TextView(this).apply { text="${intent.getIntExtra("session_count",30)} videos watched"; textSize=14f; setTextColor(Color.GRAY); setPadding(0,12,0,22) }
        val close = Button(this).apply { text="Close the app"; setOnClickListener { moveTaskToBack(true); finish() } }
        val wait = Button(this).apply { text="Give me 5 minutes (5)"; isEnabled=false; setOnClickListener { finish() } }
        root.addView(title); root.addView(dream); root.addView(hey); root.addView(count); root.addView(close); root.addView(wait)
        setContentView(root)
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        window.setGravity(Gravity.CENTER)
        if (Build.VERSION.SDK_INT >= 29) window.decorView.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
        timer = object: CountDownTimer(5000,1000) { override fun onTick(ms:Long) { wait.text="Give me 5 minutes (${(ms/1000)+1})" }; override fun onFinish() { wait.text="Give me 5 minutes"; wait.isEnabled=true } }.start()
    }
    override fun onDestroy() { timer?.cancel(); super.onDestroy() }
}

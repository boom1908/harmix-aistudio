package com.boom.harmix

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/**
 * Isolated crash diagnostic activity running in a dedicated process (:crash_handler).
 * If the main process ever crashes, this screen catches it, displays the full stack trace,
 * and allows the user to copy the log with one tap.
 */
class CrashReportActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val errorText = intent.getStringExtra(EXTRA_ERROR_DETAILS)
            ?: getSharedPreferences("harmix_crash", Context.MODE_PRIVATE).getString("last_crash", null)
            ?: "No error details available."

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
            setBackgroundColor(0xFF111111.toInt())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val title = TextView(this).apply {
            text = "Harmix Crash Report"
            textSize = 22f
            setTextColor(0xFFFFFFE3.toInt())
            setPadding(0, 0, 0, 16)
        }
        root.addView(title)

        val description = TextView(this).apply {
            text = "The app encountered an error during launch or playback. Copy this log so it can be fixed immediately:"
            textSize = 14f
            setTextColor(0xFFD9D7D2.toInt())
            setPadding(0, 0, 0, 24)
        }
        root.addView(description)

        val copyButton = Button(this).apply {
            text = "Copy Error Log"
            setBackgroundColor(0xFFFFFFE3.toInt())
            setTextColor(0xFF111111.toInt())
            setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipboard?.setPrimaryClip(ClipData.newPlainText("Harmix Crash Log", errorText))
                Toast.makeText(this@CrashReportActivity, "Copied log to clipboard!", Toast.LENGTH_SHORT).show()
            }
        }
        root.addView(copyButton)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply {
                setMargins(0, 24, 0, 24)
            }
            setBackgroundColor(0xFF1F1F1F.toInt())
            setPadding(24, 24, 24, 24)
        }

        val logView = TextView(this).apply {
            text = errorText
            setTextColor(0xFFFF8A80.toInt())
            textSize = 12f
            setTextIsSelectable(true)
        }
        scrollView.addView(logView)
        root.addView(scrollView)

        val restartButton = Button(this).apply {
            text = "Restart Harmix"
            setBackgroundColor(0xFF292929.toInt())
            setTextColor(0xFFFFFFE3.toInt())
            setOnClickListener {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(launchIntent)
                finish()
            }
        }
        root.addView(restartButton)

        setContentView(root)
    }

    companion object {
        const val EXTRA_ERROR_DETAILS = "com.boom.harmix.ERROR_DETAILS"
    }
}

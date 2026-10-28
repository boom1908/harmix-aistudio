package com.boom.harmix

import android.app.Application
import android.content.Context
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class HarmixApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Catch and persist fatal crashes for immediate diagnosis instead of silent app exit
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("HarmixCrash", "FATAL CRASH on thread ${thread.name}", throwable)
            try {
                val crashText = "${throwable::class.java.simpleName}: ${throwable.message}\n${throwable.stackTraceToString()}"
                getSharedPreferences("harmix_crash", Context.MODE_PRIVATE)
                    .edit()
                    .putString("last_crash", crashText)
                    .commit()
                File(filesDir, "last_crash.txt").writeText(crashText)
            } catch (e: Throwable) {
                Log.e("HarmixCrash", "Could not persist crash", e)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }

        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
            isReady = true
        } catch (t: Throwable) {
            Log.e("Harmix", "Failed to start Python runtime", t)
            startupError = t.message ?: t.toString()
        }
    }

    companion object {
        @Volatile
        var isReady: Boolean = false
            private set

        @Volatile
        var startupError: String? = null
            private set
    }
}

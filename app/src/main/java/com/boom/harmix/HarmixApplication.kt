package com.boom.harmix

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import kotlin.concurrent.thread

@HiltAndroidApp
class HarmixApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        // Ensure Firebase is initialized as early as possible before any services or injection
        if (base != null) {
            com.boom.harmix.auth.FirebaseInitializer.init(base)
        }

        // Install process-wide crash catcher as early as possible
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("HarmixCrash", "FATAL CRASH on thread ${thread.name}", throwable)
            val crashText = "${throwable::class.java.name}: ${throwable.message}\n${throwable.stackTraceToString()}"

            try {
                base?.getSharedPreferences("harmix_crash", Context.MODE_PRIVATE)
                    ?.edit()
                    ?.putString("last_crash", crashText)
                    ?.commit()
                if (base?.filesDir != null) {
                    File(base.filesDir, "last_crash.txt").writeText(crashText)
                }
            } catch (ignored: Throwable) {
                Log.e("HarmixCrash", "Could not persist crash", ignored)
            }

            try {
                if (base != null) {
                    val intent = Intent(base, CrashReportActivity::class.java).apply {
                        putExtra(CrashReportActivity.EXTRA_ERROR_DETAILS, crashText)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    base.startActivity(intent)
                }
            } catch (e: Throwable) {
                Log.e("HarmixCrash", "Could not launch CrashReportActivity", e)
                defaultHandler?.uncaughtException(thread, throwable)
                return@setDefaultUncaughtExceptionHandler
            }

            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(10)
        }
    }

    override fun onCreate() {
        super.onCreate()
        com.boom.harmix.auth.FirebaseInitializer.init(this)

        // Initialize Chaquopy asynchronously on a background worker thread.
        // On first run, extracting Python runtime, stdlib, yt-dlp, and wheels can take
        // 5-10 seconds on mobile devices. Running this on the main thread causes the OS
        // to kill the app with an ANR (Application Not Responding) crash before the UI appears.
        thread(name = "Harmix-PythonInit", isDaemon = true) {
            try {
                val startTime = System.currentTimeMillis()
                if (!Python.isStarted()) {
                    Python.start(AndroidPlatform(this@HarmixApplication))
                }
                isReady = true
                Log.i("Harmix", "Python runtime initialized in ${System.currentTimeMillis() - startTime}ms")
            } catch (t: Throwable) {
                Log.e("Harmix", "Failed to start Python runtime", t)
                startupError = "${t::class.java.simpleName}: ${t.message ?: t.toString()}"
            }
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

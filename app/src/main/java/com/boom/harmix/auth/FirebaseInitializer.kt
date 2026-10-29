package com.boom.harmix.auth

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp

object FirebaseInitializer {
    private const val TAG = "HarmixFirebase"

    @Synchronized
    fun init(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                return
            }

            // Standard automatic initialization from google-services.json generated resources
            val defaultApp = FirebaseApp.initializeApp(context)
            if (defaultApp != null) {
                Log.i(TAG, "FirebaseApp initialized successfully from google-services.json resources")
            } else {
                Log.e(
                    TAG,
                    "FATAL CONFIG ERROR: FirebaseApp.initializeApp(context) returned null! " +
                    "Ensure 'app/google-services.json' is present in your build repository."
                )
            }
        } catch (e: Throwable) {
            Log.e(
                TAG,
                "FATAL CONFIG ERROR: Failed to initialize FirebaseApp: ${e.message}. " +
                "Ensure 'app/google-services.json' is present and valid.",
                e
            )
        }
    }
}

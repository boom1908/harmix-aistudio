package com.boom.harmix.auth

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseInitializer {
    private const val TAG = "HarmixFirebase"

    @Synchronized
    fun init(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                return
            }

            // 1. Try standard automatic initialization from resources
            val defaultApp = FirebaseApp.initializeApp(context)
            if (defaultApp != null) {
                Log.i(TAG, "FirebaseApp initialized from resources")
                return
            }

            // 2. Fallback to explicit configuration from harmix project
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:308449061407:android:ab5abc6b0ba5464bab032a")
                .setApiKey("AIzaSyBEFcJR8J1ILby3ypnXlcoD3pDLzVWN7WY")
                .setProjectId("harmix-504905")
                .setStorageBucket("harmix-504905.firebasestorage.app")
                .setGcmSenderId("308449061407")
                .build()

            FirebaseApp.initializeApp(context.applicationContext ?: context, options)
            Log.i(TAG, "FirebaseApp successfully initialized with explicit fallback options")
        } catch (e: Throwable) {
            Log.e(TAG, "FirebaseApp initialization error: ${e.message}", e)
        }
    }
}

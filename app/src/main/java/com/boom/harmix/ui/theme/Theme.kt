package com.boom.harmix.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Ivory,
    onPrimary = PureBlack,
    secondary = SoftGrey,
    onSecondary = PureBlack,
    tertiary = Slate,
    background = PureBlack,
    onBackground = Ivory,
    surface = Charcoal,
    onSurface = Ivory,
    surfaceVariant = CharcoalSurface,
    onSurfaceVariant = SoftGrey,
    outline = GlassBorder
)

private tailrec fun android.content.Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun HarmixTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            if (activity != null) {
                val window = activity.window
                window.statusBarColor = PureBlack.toArgb()
                window.navigationBarColor = PureBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = HarmixTypography,
        content = content
    )
}

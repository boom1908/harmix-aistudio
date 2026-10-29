package com.boom.harmix.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.boom.harmix.ui.theme.Charcoal
import com.boom.harmix.ui.theme.EmberRed
import com.boom.harmix.ui.theme.GlassBorder
import com.boom.harmix.ui.theme.GlassFill
import com.boom.harmix.ui.theme.GlassSpecularBorder
import com.boom.harmix.ui.theme.Ivory
import com.boom.harmix.ui.theme.PureBlack
import com.boom.harmix.ui.theme.SoftGrey

/** Slim frosted glass strip shown at the top of the app whenever there is no internet. */
@Composable
fun OfflineBanner(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(shape)
            .background(Charcoal.copy(alpha = 0.85f))
            .border(BorderStroke(1.dp, EmberRed.copy(alpha = 0.6f)), shape)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CloudOff, contentDescription = null, tint = EmberRed, modifier = Modifier.size(18.dp))
        Text(
            text = "You're offline",
            color = Ivory,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 10.dp).weight(1f)
        )
        TextButton(onClick = onRetry) {
            Text(text = "Retry", color = Ivory, fontWeight = FontWeight.Bold)
        }
    }
}

/** Slim frosted glass strip showing sleep timer countdown in the exact same top notification position. */
@Composable
fun SleepTimerBanner(
    remainingMs: Long,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (remainingMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val timeFormatted = "%02d:%02d".format(minutes, seconds)
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(shape)
            .background(Charcoal.copy(alpha = 0.85f))
            .border(BorderStroke(1.dp, GlassSpecularBorder), shape)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Timer, contentDescription = null, tint = Ivory, modifier = Modifier.size(18.dp))
        Text(
            text = "Sleep timer: $timeFormatted",
            color = Ivory,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 10.dp).weight(1f)
        )
        TextButton(onClick = onCancel) {
            Text(text = "Turn off", color = SoftGrey, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Friendly full-panel error with a retry button. Used anywhere a fetch failed,
 * so nothing ever crashes just because the internet dropped.
 */
@Composable
fun ErrorRetryPanel(
    message: String,
    modifier: Modifier = Modifier,
    isOffline: Boolean = false,
    onRetry: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(shape)
            .background(Charcoal.copy(alpha = 0.85f))
            .border(BorderStroke(1.dp, GlassSpecularBorder), shape)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (isOffline) Icons.Filled.CloudOff else Icons.Filled.Refresh,
            contentDescription = null,
            tint = Ivory,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = if (isOffline) "No internet connection" else "Something went wrong",
            color = Ivory,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(text = message, color = SoftGrey, style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = onRetry) {
            Text(text = "Try again", color = Ivory, fontWeight = FontWeight.Bold)
        }
    }
}
package com.boom.harmix.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.boom.harmix.extractor.StreamItem
import com.boom.harmix.ui.theme.Charcoal
import com.boom.harmix.ui.theme.CharcoalElevated
import com.boom.harmix.ui.theme.CharcoalSurface
import com.boom.harmix.ui.theme.GlassBorder
import com.boom.harmix.ui.theme.GlassFloatingBarBrush
import com.boom.harmix.ui.theme.GlassSpecularBorder
import com.boom.harmix.ui.theme.Ivory
import com.boom.harmix.ui.theme.PureBlack
import com.boom.harmix.ui.theme.Sand
import com.boom.harmix.ui.theme.SoftGrey

const val ANYDOWN_PACKAGE_NAME = "com.boom.anydown"

/**
 * Extracts a standard YouTube video ID from a watch URL, short URL, or direct id.
 */
fun extractVideoId(url: String): String? {
    if (url.isBlank()) return null
    if (!url.contains("http") && url.length == 11) return url
    return runCatching {
        val uri = Uri.parse(url)
        uri.getQueryParameter("v")
            ?: if (uri.host?.contains("youtu.be") == true) uri.path?.removePrefix("/") else null
    }.getOrNull()
}

/**
 * Splits songs into chunks of up to 50 tracks and formats them as standard
 * https://www.youtube.com/watch_videos?video_ids=... playlist links that yt-dlp natively supports.
 */
fun buildShareLinks(songs: List<StreamItem>, remotePlaylistId: String? = null): List<PlaylistShareBatch> {
    if (!remotePlaylistId.isNullOrBlank()) {
        return listOf(
            PlaylistShareBatch(
                partNumber = 1,
                totalParts = 1,
                songCount = songs.size,
                url = "https://music.youtube.com/playlist?list=$remotePlaylistId"
            )
        )
    }

    val videoIds = songs.mapNotNull { extractVideoId(it.url) }
    if (videoIds.isEmpty()) return emptyList()

    val chunks = videoIds.chunked(50)
    return chunks.mapIndexed { index, batchIds ->
        val link = "https://www.youtube.com/watch_videos?video_ids=${batchIds.joinToString(",")}"
        PlaylistShareBatch(
            partNumber = index + 1,
            totalParts = chunks.size,
            songCount = batchIds.size,
            url = link
        )
    }
}

data class PlaylistShareBatch(
    val partNumber: Int,
    val totalParts: Int,
    val songCount: Int,
    val url: String
)

/**
 * Checks if Anydown (com.boom.anydown) is installed on the user's device.
 */
fun isAnydownInstalled(context: Context): Boolean {
    return runCatching {
        context.packageManager.getPackageInfo(ANYDOWN_PACKAGE_NAME, 0)
        true
    }.getOrDefault(false)
}

/**
 * Opens Anydown directly with the playlist link. If not installed, falls back
 * to standard Android Share Sheet so the user can send to any downloader or clipboard.
 */
fun sendLinkToAnydownOrShare(context: Context, link: String, playlistName: String) {
    if (isAnydownInstalled(context)) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            setPackage(ANYDOWN_PACKAGE_NAME)
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, link)
            putExtra(Intent.EXTRA_SUBJECT, playlistName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val launchIntent = context.packageManager.getLaunchIntentForPackage(ANYDOWN_PACKAGE_NAME)?.apply {
            putExtra(Intent.EXTRA_TEXT, link)
            putExtra("url", link)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            // Attempt targeted send intent first
            context.startActivity(sendIntent)
            Toast.makeText(context, "Opening in Anydown...", Toast.LENGTH_SHORT).show()
            return
        } catch (e: Exception) {
            try {
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                    Toast.makeText(context, "Opening Anydown...", Toast.LENGTH_SHORT).show()
                    return
                }
            } catch (_: Exception) { }
        }
    }

    // Fallback: Open system share chooser
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, link)
        putExtra(Intent.EXTRA_SUBJECT, playlistName)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share or download with..."))
}

@Composable
fun PlaylistShareDialog(
    playlistName: String,
    songs: List<StreamItem>,
    remotePlaylistId: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val batches = remember(songs, remotePlaylistId) { buildShareLinks(songs, remotePlaylistId) }
    val anydownAvailable = remember { isAnydownInstalled(context) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(26.dp),
            color = CharcoalSurface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, GlassSpecularBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Share & Download",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Ivory
                        )
                        Text(
                            text = playlistName,
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = SoftGrey
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                if (batches.isEmpty()) {
                    Text(
                        text = "This playlist has no exportable YouTube tracks.",
                        color = Sand,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    Text(
                        text = if (batches.size == 1) {
                            "Compatible with yt-dlp & Anydown for direct batch downloading:"
                        } else {
                            "YouTube supports up to 50 songs per batch link. Split into ${batches.size} links:"
                        },
                        color = SoftGrey.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(batches) { _, batch ->
                            PlaylistBatchItem(
                                batch = batch,
                                playlistName = playlistName,
                                anydownAvailable = anydownAvailable
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Bottom actions: Share all or Anydown status
                if (anydownAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Ivory.copy(alpha = 0.08f))
                            .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(14.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = null,
                                tint = Ivory,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Anydown detected • Tap download to send directly",
                                color = Ivory,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistBatchItem(
    batch: PlaylistShareBatch,
    playlistName: String,
    anydownAvailable: Boolean
) {
    val context = LocalContext.current
    val title = if (batch.totalParts > 1) {
        "Part ${batch.partNumber} of ${batch.totalParts} (${batch.songCount} songs)"
    } else {
        "Complete Playlist (${batch.songCount} songs)"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CharcoalElevated)
            .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Ivory,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Copy button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Harmix Playlist Link", batch.url))
                            Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy Link",
                            tint = Ivory,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Native Share button
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, batch.url)
                                putExtra(Intent.EXTRA_SUBJECT, "$playlistName (${batch.partNumber})")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Playlist link"))
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = Ivory,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Anydown direct download button
            Button(
                onClick = { sendLinkToAnydownOrShare(context, batch.url, playlistName) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (anydownAvailable) Ivory else Charcoal,
                    contentColor = if (anydownAvailable) PureBlack else Ivory
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (anydownAvailable) "Download in Anydown" else "Send to Downloader / Share",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

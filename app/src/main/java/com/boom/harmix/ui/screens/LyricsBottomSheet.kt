package com.boom.harmix.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.boom.harmix.metadata.LyricLine
import com.boom.harmix.metadata.LyricsResult
import com.boom.harmix.ui.components.KeepScreenOn
import com.boom.harmix.ui.theme.CoolGray
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalContext
import com.boom.harmix.ui.theme.Ivory
import com.boom.harmix.ui.theme.Charcoal
import com.boom.harmix.ui.theme.SoftGrey
import com.boom.harmix.ui.theme.DeepMidnight
import com.boom.harmix.ui.theme.MistWhite
import com.boom.harmix.ui.theme.ZenCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsBottomSheet(
    lyricsResult: LyricsResult?,
    currentPositionMs: Long,
    onSelectProvider: (provider: String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepMidnight,
        contentColor = MistWhite
    ) {
        KeepScreenOn()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .padding(horizontal = 20.dp)
        ) {
            // Header with provider selector and copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Provider tabs: LRCLIB, YouTube Music, OVH
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentProvider = when (lyricsResult) {
                        is LyricsResult.Synced -> lyricsResult.provider
                        is LyricsResult.PlainOnly -> lyricsResult.provider
                        else -> "Auto"
                    }

                    listOf("Auto" to "auto", "LRCLIB" to "lrclib", "YT Music" to "ytmusic", "OVH" to "ovh").forEach { (label, key) ->
                        val isSelected = currentProvider.contains(label, ignoreCase = true) || (label == "Auto" && currentProvider == "Auto")
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) Ivory.copy(alpha = 0.22f) else Charcoal.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectProvider(key) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Ivory else SoftGrey.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Copy Lyrics Action
                IconButton(
                    onClick = {
                        val textToCopy = when (lyricsResult) {
                            is LyricsResult.PlainOnly -> lyricsResult.text
                            is LyricsResult.Synced -> lyricsResult.lines.joinToString("\n") { it.text }
                            else -> null
                        }
                        if (!textToCopy.isNullOrBlank()) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Harmix Lyrics", textToCopy))
                            Toast.makeText(context, "Lyrics copied to clipboard!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No lyrics to copy", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy Lyrics",
                        tint = Ivory,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (lyricsResult) {
                    null -> Text(text = "Loading lyrics...", color = CoolGray)
                    LyricsResult.NotFound -> Text(text = "No lyrics found from this provider. Try switching tabs above.", color = CoolGray)
                    is LyricsResult.PlainOnly -> {
                        LazyColumn {
                            item {
                                Text(
                                    text = lyricsResult.text,
                                    color = MistWhite,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }
                    }
                    is LyricsResult.Synced -> SyncedLyricsList(lyricsResult.lines, currentPositionMs)
                }
            }
        }
    }
}

@Composable
private fun SyncedLyricsList(lines: List<LyricLine>, currentPositionMs: Long) {
    val listState = rememberLazyListState()
    // Keep blank timestamped entries available to buildLyricRows: LRCLIB uses
    // them as explicit instrumental-section markers.
    val lyricLines = remember(lines) { lines }
    val rows = remember(lyricLines) { buildLyricRows(lyricLines) }
    val activeIndex = lyricLines.indexOfLast {
        it.text.isNotBlank() && it.timestampMs <= currentPositionMs
    }

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            val rowIndex = rows.indexOfFirst { it is LyricRow.Line && it.index == activeIndex }
            if (rowIndex >= 0) listState.animateScrollToItem((rowIndex - 2).coerceAtLeast(0))
        }
    }

    if (rows.isEmpty()) {
        Text("No synced lyrics found for this track.", color = CoolGray)
        return
    }

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        items(rows, key = { row ->
            when (row) {
                is LyricRow.Line -> "line-${row.index}"
                is LyricRow.Gap -> "gap-${row.startMs}-${row.endMs}"
            }
        }) { row ->
            when (row) {
                is LyricRow.Line -> {
                    val isActive = row.index == activeIndex
                    Text(
                        text = row.line.text,
                        color = if (isActive) ZenCyan else CoolGray,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        style = if (isActive) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is LyricRow.Gap -> InstrumentalGap(row, currentPositionMs)
            }
        }
    }
}

private sealed interface LyricRow {
    data class Line(val index: Int, val line: LyricLine) : LyricRow
    data class Gap(val startMs: Long, val endMs: Long) : LyricRow
}

private fun buildLyricRows(lines: List<LyricLine>): List<LyricRow> {
    if (lines.isEmpty()) return emptyList()
    val rows = mutableListOf<LyricRow>()
    var instrumentalMarkerMs: Long? = null

    lines.forEachIndexed { index, line ->
        if (line.text.isBlank()) {
            if (instrumentalMarkerMs == null) {
                instrumentalMarkerMs = line.timestampMs
            }
            return@forEachIndexed
        }

        instrumentalMarkerMs?.let { gapStartMs ->
            val gapDurationMs = line.timestampMs - gapStartMs
            if (gapDurationMs > GAP_THRESHOLD_MS) {
                rows += LyricRow.Gap(gapStartMs, line.timestampMs)
            }
        }
        rows += LyricRow.Line(index, line)
        instrumentalMarkerMs = null
    }
    return rows
}

@Composable
private fun InstrumentalGap(gap: LyricRow.Gap, currentPositionMs: Long) {
    val progress = ((currentPositionMs - gap.startMs).toFloat() / (gap.endMs - gap.startMs))
        .coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(progress, label = "instrumental-gap-progress")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Canvas(modifier = Modifier.size(42.dp)) {
            val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = CoolGray.copy(alpha = 0.25f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            drawArc(
                color = ZenCyan,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = stroke
            )
        }
        val duration = formatGapTime(gap.endMs - gap.startMs)
        val elapsed = formatGapTime((currentPositionMs - gap.startMs).coerceIn(0L, gap.endMs - gap.startMs))
        Text(
            text = "Instrumental · $elapsed / $duration",
            color = CoolGray,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun formatGapTime(millis: Long): String {
    val totalSeconds = millis.coerceAtLeast(0L) / 1000L
    return "%d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
}

private const val GAP_THRESHOLD_MS = 4_000L
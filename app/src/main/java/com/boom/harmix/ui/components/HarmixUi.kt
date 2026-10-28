package com.boom.harmix.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.boom.harmix.R
import com.boom.harmix.ui.theme.Charcoal
import com.boom.harmix.ui.theme.CharcoalElevated
import com.boom.harmix.ui.theme.GlassBorder
import com.boom.harmix.ui.theme.GlassBorderSubtle
import com.boom.harmix.ui.theme.GlassFill
import com.boom.harmix.ui.theme.GlassFillSubtle
import com.boom.harmix.ui.theme.Ivory
import com.boom.harmix.ui.theme.PureBlack
import com.boom.harmix.ui.theme.Slate
import com.boom.harmix.ui.theme.SoftGrey

/** Refined Ivory / Soft Grey gradient accent. */
val AccentBrush: Brush = Brush.linearGradient(listOf(Ivory, SoftGrey))
val SunsetBrush: Brush = AccentBrush

/** Soft atmospheric glow behind headers. */
val HeaderGlow: Brush = Brush.verticalGradient(
    listOf(Charcoal.copy(alpha = 0.45f), Color.Transparent)
)

@Composable
fun HarmixLogo(size: Int = 36, modifier: Modifier = Modifier) {
    AsyncImage(
        model = R.drawable.harmix_logo,
        contentDescription = "Harmix",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(1.dp, GlassBorder, CircleShape)
    )
}

/** Page header with gradient atmosphere, Ivory display title, and optional subtitle. */
@Composable
fun PageHeader(
    title: String,
    subtitle: String? = null,
    showLogo: Boolean = true,
    trailing: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderGlow)
            .padding(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (showLogo) {
                HarmixLogo()
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Ivory,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (trailing != null) trailing()
        }
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Ivory,
            modifier = Modifier.weight(1f)
        )
        if (action != null && onAction != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelLarge,
                color = SoftGrey,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

/** Sleek minimal glass card with subtle border and smoked Charcoal backdrop. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Charcoal.copy(alpha = 0.80f), Charcoal.copy(alpha = 0.60f))
                )
            )
            .border(BorderStroke(1.dp, GlassBorder), shape)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

/** Solid Ivory pill button with pure black text for high-contrast primary actions. */
@Composable
fun GoldPillButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(46.dp)
            .clip(CircleShape)
            .background(Ivory)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = PureBlack, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, color = PureBlack, fontWeight = FontWeight.Black)
    }
}

/** Outlined glass pill button for secondary actions. */
@Composable
fun GhostPillButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(46.dp)
            .clip(CircleShape)
            .background(GlassFill)
            .border(BorderStroke(1.dp, GlassBorder), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Ivory, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, color = Ivory, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun HarmixChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .then(
                if (selected) Modifier.background(Ivory)
                else Modifier.background(GlassFillSubtle).border(1.dp, GlassBorderSubtle, CircleShape)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (selected) PureBlack else SoftGrey,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

/** Square artwork with a graceful fallback when there's no thumbnail. */
@Composable
fun Artwork(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(14.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(listOf(CharcoalElevated, PureBlack)))
    ) {
        if (url.isNullOrBlank()) {
            Icon(
                Icons.Filled.MusicNote,
                contentDescription = contentDescription,
                tint = Ivory.copy(alpha = .6f),
                modifier = Modifier.align(Alignment.Center).size(22.dp)
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** Vertical track row: artwork, title, artist, optional index and overflow menu. */
@Composable
fun TrackRow(
    title: String,
    subtitle: String,
    artworkUrl: String?,
    modifier: Modifier = Modifier,
    index: Int? = null,
    active: Boolean = false,
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    isLiked: Boolean = false,
    onLikeClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .then(
                if (active) Modifier
                    .background(GlassFill)
                    .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(18.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (index != null) {
            Text(
                text = "${index + 1}".padStart(2, '0'),
                color = if (active) Ivory else Slate,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp)
            )
        }
        Artwork(artworkUrl, title, modifier = Modifier.size(52.dp))
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                text = title,
                color = if (active) Ivory else SoftGrey,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle.ifBlank { "Unknown artist" },
                color = Slate,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onLikeClick != null) {
            IconButton(onClick = onLikeClick) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isLiked) "Remove from liked songs" else "Add to liked songs",
                    tint = if (isLiked) Ivory else Slate
                )
            }
        }
        if (onMoreClick != null) {
            IconButton(onClick = onMoreClick) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = Slate)
            }
        }
    }
}

/** Big square card used inside horizontal shelves. */
@Composable
fun MediaCard(
    title: String,
    subtitle: String,
    artworkUrl: String?,
    modifier: Modifier = Modifier,
    round: Boolean = false,
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    isLiked: Boolean = false,
    onLikeClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .width(148.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box {
            Artwork(
                artworkUrl,
                title,
                shape = if (round) CircleShape else RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Ivory),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = PureBlack, modifier = Modifier.size(20.dp))
            }
            if (onLikeClick != null || onMoreClick != null) {
                Row(
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (onLikeClick != null) {
                        IconButton(
                            onClick = onLikeClick,
                            modifier = Modifier
                                .size(34.dp)
                                .background(PureBlack.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (isLiked) "Remove from liked songs" else "Add to liked songs",
                                tint = if (isLiked) Ivory else SoftGrey,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (onMoreClick != null) {
                        IconButton(
                            onClick = onMoreClick,
                            modifier = Modifier
                                .size(34.dp)
                                .background(PureBlack.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "More options",
                                tint = SoftGrey,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
        Text(
            text = title,
            color = Ivory,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 10.dp)
        )
        Text(
            text = subtitle,
            color = Slate,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Horizontally scrolling shelf of media cards. */
@Composable
fun <T> Shelf(
    title: String,
    items: List<T>,
    modifier: Modifier = Modifier,
    card: @Composable (T) -> Unit
) {
    if (items.isEmpty()) return
    Column(modifier = modifier) {
        SectionTitle(title)
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item -> card(item) }
        }
    }
}

@Composable
fun EmptyState(title: String, detail: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(GlassFillSubtle)
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(22.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = title, color = Ivory, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = detail, color = Slate, style = MaterialTheme.typography.bodySmall)
    }
}

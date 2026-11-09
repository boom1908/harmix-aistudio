package com.boom.harmix.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boom.harmix.extractor.StreamItem
import com.boom.harmix.ui.components.Artwork
import com.boom.harmix.ui.components.EmptyState
import com.boom.harmix.ui.components.MediaCard
import com.boom.harmix.ui.components.PageHeader
import com.boom.harmix.ui.components.SectionTitle
import com.boom.harmix.ui.components.Shelf
import com.boom.harmix.ui.components.TrackRow
import com.boom.harmix.ui.theme.Charcoal
import com.boom.harmix.ui.theme.GlassBorder
import com.boom.harmix.ui.theme.GlassBorderSubtle
import com.boom.harmix.ui.theme.GlassCardBrush
import com.boom.harmix.ui.theme.GlassFloatingBarBrush
import com.boom.harmix.ui.theme.GlassSpecularBorder
import com.boom.harmix.ui.theme.Ivory
import com.boom.harmix.ui.theme.PureBlack
import com.boom.harmix.ui.theme.Sand
import com.boom.harmix.ui.theme.Slate
import com.boom.harmix.ui.theme.SoftGrey
import com.boom.harmix.ui.viewmodel.ArtistUi
import com.boom.harmix.ui.viewmodel.HomeUiState
import com.boom.harmix.ui.viewmodel.HomeViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    isGuest: Boolean,
    username: String?,
    onItemClick: (StreamItem) -> Unit,
    onPlayQueue: (List<StreamItem>, Int) -> Unit = { list, idx -> list.getOrNull(idx)?.let(onItemClick) },
    onPlayNext: (StreamItem) -> Unit,
    onAddToQueue: (StreamItem) -> Unit,
    onAddToPlaylistRequest: (StreamItem) -> Unit,
    likedUrls: Set<String>,
    onToggleLike: (StreamItem) -> Unit,
    onOpenPlaylist: (Long) -> Unit = {},
    onOpenArtist: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var optionsSheetTarget by remember { mutableStateOf<StreamItem?>(null) }
    val context = LocalContext.current

    LaunchedEffect(isGuest) { viewModel.loadRecommendations() }

    Column(modifier = Modifier.fillMaxSize()) {
        PageHeader(
            title = greetingForCurrentTime() + username?.let { ", $it" }.orEmpty(),
            subtitle = if (username != null) "Personalized daily mix & what's trending"
            else "Here's what's trending right now"
        )

        when (val state = uiState) {
            is HomeUiState.Loading -> Text(
                text = "Loading recommendations...",
                color = Sand,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            is HomeUiState.Error -> com.boom.harmix.ui.components.ErrorRetryPanel(
                message = state.message,
                isOffline = state.offline,
                onRetry = { viewModel.retry() }
            )

            is HomeUiState.Success -> {
                if (state.items.isEmpty()) {
                    EmptyState(
                        title = "Nothing trending right now",
                        detail = "Pull up a search and start listening — your feed will fill up."
                    )
                } else if (isGuest) {
                    // GUEST MODE: Kept exactly as it was
                    val featured = state.items.take(8)
                    val rest = state.items.drop(8)
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            Shelf(
                                title = "Trending tracks",
                                items = featured
                            ) { item ->
                                MediaCard(
                                    title = item.title,
                                    subtitle = item.uploader.ifBlank { "Unknown artist" },
                                    artworkUrl = item.thumbnailUrl,
                                    onClick = { onItemClick(item) },
                                    onMoreClick = { optionsSheetTarget = item },
                                    isLiked = item.url in likedUrls,
                                    onLikeClick = null
                                )
                            }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                        item { SectionTitle("Trending now") }
                        items(rest.ifEmpty { state.items }) { item ->
                            TrackRow(
                                title = item.title,
                                subtitle = item.uploader,
                                artworkUrl = item.thumbnailUrl,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                onClick = { onItemClick(item) },
                                onMoreClick = { optionsSheetTarget = item },
                                isLiked = item.url in likedUrls,
                                onLikeClick = null
                            )
                        }
                    }
                } else {
                    // AUTHENTICATED USER MODE: Redesigned matching HTML model
                    UserHomeContent(
                        dailyMix = state.dailyMix,
                        recentPlaylists = state.recentPlaylists,
                        quickPicks = state.forYou.ifEmpty { state.items },
                        trending = state.items,
                        artists = state.topArtists,
                        likedUrls = likedUrls,
                        onItemClick = onItemClick,
                        onPlayQueue = onPlayQueue,
                        onMoreClick = { optionsSheetTarget = it },
                        onToggleLike = onToggleLike,
                        onOpenPlaylist = onOpenPlaylist,
                        onOpenArtist = onOpenArtist,
                        onSaveDailyMix = {
                            viewModel.savePlaylist("Daily Mix - ${todayString()}", state.dailyMix) {
                                Toast.makeText(context, "Daily Mix saved to Library!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }

    optionsSheetTarget?.let { target ->
        SongOptionsBottomSheet(
            item = target,
            isGuest = isGuest,
            onDismiss = { optionsSheetTarget = null },
            onPlayNext = onPlayNext,
            onAddToQueue = onAddToQueue,
            onAddToPlaylistRequest = onAddToPlaylistRequest
        )
    }
}

@Composable
private fun UserHomeContent(
    dailyMix: List<StreamItem>,
    recentPlaylists: List<com.boom.harmix.data.local.PlaylistUi>,
    quickPicks: List<StreamItem>,
    trending: List<StreamItem>,
    artists: List<ArtistUi>,
    likedUrls: Set<String>,
    onItemClick: (StreamItem) -> Unit,
    onPlayQueue: (List<StreamItem>, Int) -> Unit,
    onMoreClick: (StreamItem) -> Unit,
    onToggleLike: (StreamItem) -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    onOpenArtist: (String) -> Unit,
    onSaveDailyMix: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HERO ROW: Daily Mix Card (58% width, 2x2 collage) + Side Stack of 3 playlists
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(206.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Daily Mix Card (58% width)
                DailyMixCard(
                    songs = dailyMix,
                    modifier = Modifier
                        .weight(0.58f)
                        .fillMaxHeight(),
                    onPlay = { if (dailyMix.isNotEmpty()) onPlayQueue(dailyMix, 0) },
                    onSave = onSaveDailyMix
                )

                // Right Side: 3 stacked compact playlist cards
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sideItems = recentPlaylists.take(3)
                    if (sideItems.isNotEmpty()) {
                        sideItems.forEach { pl ->
                            CompactPlaylistCard(
                                playlist = pl,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                onClick = { onOpenPlaylist(pl.id) }
                            )
                        }
                    } else {
                        // Fallback placeholder card if library is brand new
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(GlassFloatingBarBrush)
                                .border(BorderStroke(1.dp, GlassSpecularBorder), RoundedCornerShape(20.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Your library will appear here", color = Sand, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2. QUICK PICKS: Paginated, 4 songs per page (Horizontal Pager)
        if (quickPicks.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick picks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Ivory
                    )
                    TextButton(onClick = { onPlayQueue(quickPicks, 0) }) {
                        Text("Play all", color = Ivory, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                val pages = quickPicks.chunked(4)
                val pagerState = rememberPagerState(pageCount = { pages.size })

                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    pageSpacing = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) { pageIndex ->
                    val pageSongs = pages[pageIndex]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .background(GlassFloatingBarBrush)
                            .border(BorderStroke(1.dp, GlassSpecularBorder), RoundedCornerShape(26.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        pageSongs.forEach { song ->
                            QuickPickSongRow(
                                song = song,
                                onClick = { onItemClick(song) },
                                onMoreClick = { onMoreClick(song) }
                            )
                        }
                    }
                }
            }
        }

        // 3. TRENDING NOW: Ranked Glass Chart with Numbers
        item {
            SectionTitle("Trending now")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(GlassFloatingBarBrush)
                    .border(BorderStroke(1.dp, GlassSpecularBorder), RoundedCornerShape(26.dp))
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                trending.take(6).forEachIndexed { index, song ->
                    RankedTrackRow(
                        rank = index + 1,
                        song = song,
                        isLiked = song.url in likedUrls,
                        onClick = { onItemClick(song) },
                        onToggleLike = { onToggleLike(song) }
                    )
                }
            }
        }

        // 4. ARTISTS YOU LIKE: Round Avatars in a Horizontal Row
        if (artists.isNotEmpty()) {
            item {
                SectionTitle("Artists you like")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(artists) { artist ->
                        ArtistAvatarItem(
                            artist = artist,
                            onClick = { onOpenArtist(artist.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyMixCard(
    songs: List<StreamItem>,
    modifier: Modifier = Modifier,
    onPlay: () -> Unit,
    onSave: () -> Unit
) {
    val shape = RoundedCornerShape(30.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .border(BorderStroke(1.dp, GlassSpecularBorder), shape)
            .background(PureBlack)
    ) {
        // 2x2 Collage of first 4 songs
        val collageItems = songs.take(4)
        if (collageItems.size >= 4) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Artwork(
                        url = collageItems[0].thumbnailUrl,
                        contentDescription = collageItems[0].title,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    Spacer(Modifier.width(2.dp))
                    Artwork(
                        url = collageItems[1].thumbnailUrl,
                        contentDescription = collageItems[1].title,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Artwork(
                        url = collageItems[2].thumbnailUrl,
                        contentDescription = collageItems[2].title,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    Spacer(Modifier.width(2.dp))
                    Artwork(
                        url = collageItems[3].thumbnailUrl,
                        contentDescription = collageItems[3].title,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        } else {
            Artwork(
                url = songs.firstOrNull()?.thumbnailUrl,
                contentDescription = "Daily mix",
                modifier = Modifier.fillMaxSize()
            )
        }

        // Equalizer indicator on top-left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PureBlack.copy(alpha = 0.65f))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            EqualizerBars()
        }

        // Save Daily Mix button on top-right
        IconButton(
            onClick = onSave,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(PureBlack.copy(alpha = 0.65f))
        ) {
            Icon(
                imageVector = Icons.Filled.BookmarkAdd,
                contentDescription = "Save Daily Mix",
                tint = Ivory,
                modifier = Modifier.size(18.dp)
            )
        }

        // Glass caption bar at bottom with play button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(GlassFloatingBarBrush)
                .border(BorderStroke(1.dp, GlassSpecularBorder), RoundedCornerShape(22.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily mix",
                        color = Ivory,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        letterSpacing = (-0.02).sp
                    )
                    Text(
                        text = "Fresh every morning",
                        color = SoftGrey,
                        fontSize = 11.5.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Ivory)
                        .clickable(onClick = onPlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play daily mix",
                        tint = PureBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactPlaylistCard(
    playlist: com.boom.harmix.data.local.PlaylistUi,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(GlassFloatingBarBrush)
            .border(BorderStroke(1.dp, GlassSpecularBorder), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val artworkUrl = playlist.songs.firstOrNull()?.thumbnailUrl
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Charcoal)
        ) {
            Artwork(
                url = artworkUrl,
                contentDescription = playlist.name,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = playlist.name,
            color = Ivory,
            fontWeight = FontWeight.Bold,
            fontSize = 12.5.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickPickSongRow(
    song: StreamItem,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Charcoal)
        ) {
            Artwork(
                url = song.thumbnailUrl,
                contentDescription = song.title,
                modifier = Modifier.fillMaxSize()
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Ivory,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.uploader,
                color = SoftGrey.copy(alpha = 0.75f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onMoreClick, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Options",
                tint = SoftGrey.copy(alpha = 0.65f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun RankedTrackRow(
    rank: Int,
    song: StreamItem,
    isLiked: Boolean,
    onClick: () -> Unit,
    onToggleLike: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Rank number
        Text(
            text = rank.toString(),
            color = if (rank <= 3) Ivory else Ivory.copy(alpha = 0.28f),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            modifier = Modifier.width(28.dp)
        )
        // 48dp artwork
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Charcoal)
        ) {
            Artwork(
                url = song.thumbnailUrl,
                contentDescription = song.title,
                modifier = Modifier.fillMaxSize()
            )
        }
        // Title & artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Ivory,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.uploader,
                color = SoftGrey.copy(alpha = 0.75f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // Duration
        val durationFormatted = formatSeconds(song.durationSeconds ?: 0)
        if (durationFormatted.isNotEmpty()) {
            Text(
                text = durationFormatted,
                color = SoftGrey.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
        // Heart Like Button
        IconButton(onClick = onToggleLike, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) Ivory else SoftGrey.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ArtistAvatarItem(
    artist: ArtistUi,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(92.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(Charcoal)
                .border(BorderStroke(1.dp, GlassSpecularBorder), CircleShape)
        ) {
            Artwork(
                url = artist.avatarUrl,
                contentDescription = artist.name,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = artist.name,
            color = Ivory,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EqualizerBars() {
    val transition = rememberInfiniteTransition(label = "eq")
    val heights = listOf(
        transition.animateFloat(0.3f, 0.7f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "h1"),
        transition.animateFloat(0.2f, 0.95f, infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "h2"),
        transition.animateFloat(0.4f, 0.6f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "h3"),
        transition.animateFloat(0.2f, 1.0f, infiniteRepeatable(tween(380), RepeatMode.Reverse), label = "h4"),
        transition.animateFloat(0.3f, 0.8f, infiniteRepeatable(tween(480), RepeatMode.Reverse), label = "h5")
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(14.dp)
    ) {
        heights.forEach { anim ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(anim.value)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Ivory)
            )
        }
    }
}

private fun formatSeconds(seconds: Int): String {
    if (seconds <= 0) return ""
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

private fun todayString(): String =
    java.text.SimpleDateFormat("MMM d", java.util.Locale.US).format(java.util.Date())

private fun greetingForCurrentTime(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

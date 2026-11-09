package com.boom.harmix.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.boom.harmix.extractor.StreamItem
import com.boom.harmix.metadata.MetadataRepository
import com.boom.harmix.ui.components.Artwork
import com.boom.harmix.ui.components.GhostPillButton
import com.boom.harmix.ui.components.GoldPillButton
import com.boom.harmix.ui.components.TrackRow
import com.boom.harmix.ui.theme.Bone
import com.boom.harmix.ui.theme.Charcoal
import com.boom.harmix.ui.theme.GlassBorder
import com.boom.harmix.ui.theme.GlassSpecularBorder
import com.boom.harmix.ui.theme.Ivory
import com.boom.harmix.ui.theme.Sand
import kotlinx.coroutines.launch

@Composable
fun ArtistTopTracksScreen(
    artistName: String,
    metadataRepository: MetadataRepository,
    onBack: () -> Unit,
    onPlayQueue: (List<StreamItem>, Int) -> Unit,
    onAddToQueue: (StreamItem) -> Unit,
    onSaveToPlaylist: (playlistName: String, songs: List<StreamItem>) -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var songs by remember { mutableStateOf<List<StreamItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(artistName) {
        isLoading = true
        try {
            songs = metadataRepository.getArtistTopTracks(artistName, limit = 25)
        } catch (e: Exception) {
            songs = emptyList()
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Charcoal)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Ivory
                )
            }
            Text(
                text = artistName,
                style = MaterialTheme.typography.titleLarge,
                color = Ivory,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
            if (songs.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onSaveToPlaylist(artistName, songs)
                        Toast.makeText(context, "Saved to '$artistName' playlist!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.BookmarkAdd,
                        contentDescription = "Save to Playlist",
                        tint = Ivory
                    )
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Ivory)
            }
            return
        }

        if (songs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No tracks found for $artistName", color = Sand)
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Artist Hero Avatar + Title + Actions
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val avatarUrl = songs.firstOrNull()?.thumbnailUrl
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(Charcoal)
                            .border(BorderStroke(1.5.dp, GlassSpecularBorder), CircleShape)
                    ) {
                        Artwork(
                            url = avatarUrl,
                            contentDescription = artistName,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Ivory,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Top Tracks • ${songs.size} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Sand
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GoldPillButton(
                            text = "Play All",
                            icon = Icons.Filled.PlayArrow,
                            onClick = { onPlayQueue(songs, 0) }
                        )
                        GhostPillButton(
                            text = "Shuffle",
                            icon = Icons.Filled.Shuffle,
                            onClick = { onPlayQueue(songs.shuffled(), 0) }
                        )
                    }
                }
            }

            itemsIndexed(songs) { index, song ->
                TrackRow(
                    title = song.title,
                    subtitle = song.uploader,
                    artworkUrl = song.thumbnailUrl,
                    onClick = { onPlayQueue(songs, index) },
                    onMoreClick = { onAddToQueue(song) },
                    isLiked = false
                )
            }
        }
    }
}

package com.boom.harmix.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boom.harmix.auth.UserSession
import com.boom.harmix.auth.UserSessionRepository
import com.boom.harmix.data.cloud.ListeningHistoryRepository
import com.boom.harmix.data.local.LibraryRepository
import com.boom.harmix.extractor.StreamItem
import com.boom.harmix.metadata.MetadataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.boom.harmix.HarmixApplication
import kotlinx.coroutines.delay

data class ArtistUi(
    val name: String,
    val avatarUrl: String? = null
)

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(
        val items: List<StreamItem>,
        val forYou: List<StreamItem> = emptyList(),
        val dailyMix: List<StreamItem> = emptyList(),
        val topArtists: List<ArtistUi> = emptyList(),
        val recentPlaylists: List<com.boom.harmix.data.local.PlaylistUi> = emptyList()
    ) : HomeUiState()
    data class Error(val message: String, val offline: Boolean = false) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val metadataRepository: MetadataRepository,
    private val libraryRepository: LibraryRepository,
    private val listeningHistoryRepository: ListeningHistoryRepository,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loaded = false
    private var loadedForSignedInUser: Boolean? = null

    fun retry() {
        loaded = false
        loadedForSignedInUser = null
        loadRecommendations()
    }

    fun loadRecommendations() {
        val authenticated = userSessionRepository.session.value as? UserSession.Authenticated
        val signedIn = authenticated != null
        if (loaded && loadedForSignedInUser == signedIn) return
        loaded = true
        loadedForSignedInUser = signedIn

        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                // Wait for background Python engine initialization (up to 12s on cold install)
                var elapsedMs = 0L
                while (!HarmixApplication.isReady && HarmixApplication.startupError == null && elapsedMs < 12_000L) {
                    delay(250)
                    elapsedMs += 250
                }

                if (HarmixApplication.startupError != null) {
                    _uiState.value = HomeUiState.Error("Audio engine alert: ${HarmixApplication.startupError}")
                    return@launch
                }

                val trending = metadataRepository.getTrending()
                if (authenticated != null) {
                    val uid = authenticated.uid
                    val liked = libraryRepository.getLikedSongs().first()
                    val history = listeningHistoryRepository.getRecentHistory(uid).first()
                    val userPlaylists = libraryRepository.getPlaylists().first()

                    // Extract personalized artist seeds
                    val artistFrequencies = (liked.map { it.uploader } + history.map { it.song.uploader })
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .groupBy { it.lowercase() }
                        .entries
                        .sortedByDescending { it.value.size }

                    val artistNames = artistFrequencies.map { it.value.first() }

                    // Daily Mix with calendar date seed (YYYY-MM-DD)
                    val dateSeed = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                    val dailyMix = metadataRepository.getDailyMix(artistNames, dateSeed, limit = 20)

                    // For You tracks (fallback / quick picks)
                    val forYou = if (artistNames.isNotEmpty()) {
                        metadataRepository.getForYou(artistNames.take(5), limit = 16)
                    } else {
                        emptyList()
                    }

                    // Top artists for the bottom shelf
                    val topArtists = artistNames.take(10).map { name ->
                        // Grab avatar from liked/history song if available
                        val avatar = (liked.firstOrNull { it.uploader.equals(name, ignoreCase = true) }
                            ?: history.firstOrNull { it.song.uploader.equals(name, ignoreCase = true) }?.song)?.thumbnailUrl
                        ArtistUi(name = name, avatarUrl = avatar)
                    }

                    // Side stack playlists: Liked Songs at #1, plus user playlists
                    val sidePlaylists = mutableListOf<com.boom.harmix.data.local.PlaylistUi>()
                    if (liked.isNotEmpty()) {
                        sidePlaylists.add(
                            com.boom.harmix.data.local.PlaylistUi(
                                id = -1L,
                                name = "Liked songs",
                                songs = liked
                            )
                        )
                    }
                    sidePlaylists.addAll(userPlaylists.take(3))

                    _uiState.value = HomeUiState.Success(
                        items = trending,
                        forYou = forYou.ifEmpty { trending },
                        dailyMix = dailyMix.ifEmpty { trending.take(16) },
                        topArtists = topArtists,
                        recentPlaylists = sidePlaylists.take(3)
                    )
                } else {
                    _uiState.value = if (trending.isEmpty()) {
                        HomeUiState.Error("YouTube Music returned no trending tracks")
                    } else {
                        HomeUiState.Success(trending)
                    }
                }
            } catch (e: com.boom.harmix.core.OfflineException) {
                _uiState.value = HomeUiState.Error(e.message ?: "You're offline.", offline = true)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Unknown error fetching recommendations")
            }
        }
    }

    fun savePlaylist(name: String, songs: List<StreamItem>, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val playlistId = libraryRepository.createPlaylist(name)
            songs.forEach { song ->
                libraryRepository.addSongToPlaylistIfAbsent(playlistId, song)
            }
            onSaved(playlistId)
        }
    }
}

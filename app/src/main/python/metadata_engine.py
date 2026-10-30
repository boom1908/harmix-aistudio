import json
import re
import requests
from ytmusicapi import YTMusic

_ytmusic = YTMusic()
_GLOBAL_TOP_SONGS_PLAYLIST_ID = "PL4fGSI1pDJn6puJdseH2Rt9sMvt9E2M4i"

def _duration_seconds(entry):
    if entry.get("duration_seconds") is not None:
        return entry.get("duration_seconds")
    raw = entry.get("duration") or entry.get("length")
    if not raw or not isinstance(raw, str) or ":" not in raw:
        return None
    try:
        parts = [int(p) for p in raw.split(":")]
        seconds = 0
        for part in parts:
            seconds = seconds * 60 + part
        return seconds
    except (ValueError, TypeError):
        return None

def _thumbnail_url(thumbnails):
    if not thumbnails:
        return None
    url = thumbnails[-1]["url"]
    high_res_url = re.sub(r"=w\d+-h\d+.*$", "=w1080-h1080-l90-rj", url)
    return high_res_url

def _artist_name(artists):
    return artists[0]["name"] if artists and artists[0].get("name") else ""

def get_up_next(video_id: str, limit: int = 10) -> str:
    result = _ytmusic.get_watch_playlist(videoId=video_id, limit=limit + 5)
    tracks = result.get("tracks", [])

    up_next = []
    for track in tracks:
        track_video_id = track.get("videoId")
        if not track_video_id or track_video_id == video_id:
            continue
        up_next.append({
            "videoId": track_video_id,
            "title": track.get("title", "Unknown title"),
            "artist": _artist_name(track.get("artists")),
            "thumbnailUrl": _thumbnail_url(track.get("thumbnail") or []),
            "durationSeconds": _duration_seconds(track),
        })
        if len(up_next) >= limit:
            break
    return json.dumps(up_next)

def _entry_to_item(entry):
    video_id = entry.get("videoId")
    if not video_id:
        return None
    # Check if item is an official album track or from a Topic/verified channel
    album = entry.get("album")
    has_album = bool(album and isinstance(album, dict) and album.get("name"))
    artists = entry.get("artists") or []
    is_topic = any("topic" in (a.get("name") or "").lower() for a in artists)
    result_type = entry.get("resultType") or ""
    category = entry.get("category") or ""
    
    return {
        "videoId": video_id,
        "title": entry.get("title", "Unknown title"),
        "artist": _artist_name(artists) or (entry.get("author") or ""),
        "thumbnailUrl": _thumbnail_url(entry.get("thumbnails") or entry.get("thumbnail") or []),
        "durationSeconds": _duration_seconds(entry),
        "_hasAlbum": has_album,
        "_isTopic": is_topic,
        "_resultType": result_type,
        "_isTopResult": category.lower() == "top result" or entry.get("isTopResult") is True,
    }

def _clean_comparison_text(value):
    """Strip noise tags like (Official Audio), [HD], (Visualizer) for clean title matching."""
    val = value or ""
    val = re.sub(r"\((official\s*(audio|music\s*video|video|visualizer|lyric\s*video|lyrics|hd|4k)?|audio|video|lyrics)\)", "", val, flags=re.I)
    val = re.sub(r"\[(official\s*(audio|music\s*video|video|visualizer|lyric\s*video|lyrics|hd|4k)?|audio|video|lyrics|hd|4k)\]", "", val, flags=re.I)
    return _normalized_search_text(val)

def _normalized_search_text(value):
    # Keep Unicode letters so Hindi and other non-Latin searches are ranked
    # instead of collapsing to an empty query.
    return re.sub(r"[^\w]+", " ", (value or "").lower(), flags=re.UNICODE).strip()

# Keywords that indicate derivative, lame, or secondary uploads unless explicitly queried
_NOISE_KEYWORDS = {
    # Heavy penalty: user rarely wants these when searching a song title
    "cover": 55,
    "parody": 60,
    "karaoke": 55,
    "instrumental": 50,
    "tribute": 55,
    "slowed": 50,
    "reverb": 45,
    "nightcore": 55,
    "8d": 50,
    "bass boosted": 50,
    "bassboosted": 50,
    "pitch shifted": 50,
    "chipmunk": 60,
    # Medium penalty: unofficial re-uploads or alternate styles
    "remix": 35,
    "acoustic cover": 55,
    "fingerstyle": 45,
    "tutorial": 60,
    "guitar lesson": 60,
    "reaction": 65,
    "status": 40,
    "short": 40,
    "shorts": 40,
    "1 hour": 50,
    "1hour": 50,
    "loop": 40,
}

def _score(item, query, position, source_bonus):
    """
    Rank items to prioritize original releases and verified master tracks over
    fan-made re-uploads, covers, and derivatives.
    """
    raw_q = (query or "").lower().strip()
    q = _normalized_search_text(query)
    tokens = [t for t in q.split() if t]
    
    raw_title = item.get("title", "")
    raw_artist = item.get("artist", "")
    title = _normalized_search_text(raw_title)
    clean_title = _clean_comparison_text(raw_title)
    artist = _normalized_search_text(raw_artist)
    haystack = (title + " " + artist).strip()
    clean_haystack = (clean_title + " " + artist).strip()

    score = source_bonus

    # 1. YouTube Music Native Top Result bonus
    if item.get("_isTopResult") or (position == 0 and source_bonus == 35):
        score += 65

    # 2. Official audio track bonus (presence of album, Topic channel, or 'song' resultType)
    if item.get("_hasAlbum"):
        score += 40
    if item.get("_isTopic"):
        score += 35
    if item.get("_resultType") == "song":
        score += 30

    # 3. Exact Query & Clean Title Matching
    # Check if user query matches the clean title (ignoring "(Official Video)" etc.)
    if q:
        if q == clean_title:
            # Absolute exact title match
            score += 100
        elif clean_title.startswith(q):
            score += 60
        elif q in clean_title:
            score += 45
        elif q in clean_haystack:
            score += 35

    # 4. Smart Title + Artist Disambiguation
    # If the user typed "Ed Sheeran Shape of You", matching artist + song title separately
    # should give maximum relevance without needing the song title to repeat the artist's name
    if artist and tokens:
        artist_tokens = [t for t in artist.split() if t]
        matched_artist = sum(1 for t in tokens if t in artist_tokens or t in artist)
        matched_title = sum(1 for t in tokens if t in clean_title)
        if matched_artist > 0 and matched_title > 0:
            # Query contains components of both artist name AND song title!
            score += 55 + (20 * min(matched_artist, 2)) + (20 * min(matched_title, 3))
        else:
            # General token matching
            score += 18 * matched_title
            matched_total = sum(1 for t in tokens if t in haystack)
            score += 25 * (matched_total / len(tokens))
            if matched_total == len(tokens):
                score += 20
    elif tokens:
        matched_total = sum(1 for t in tokens if t in haystack)
        score += 30 * (matched_total / len(tokens))

    # 5. Noise & Derivative Penalties (Covers, Slowed+Reverb, Nightcore, Karaoke, etc.)
    title_lower = raw_title.lower()
    for noise_kw, penalty in _NOISE_KEYWORDS.items():
        if noise_kw in title_lower and noise_kw not in raw_q:
            score -= penalty

    # Extra penalty for obvious cover artists in title/uploader
    if ("cover by" in title_lower or "ft. cover" in title_lower or "by cover" in title_lower) and "cover" not in raw_q:
        score -= 50

    # 6. Song duration heuristics: Standard songs are typically 90s - 480s (1.5 - 8 mins)
    duration = item.get("durationSeconds") or 0
    if 90 <= duration <= 420:
        score += 15
    elif 60 <= duration <= 600:
        score += 5
    elif duration > 0 and duration < 50:
        # Extremely short snippet/preview
        score -= 35
    elif duration > 1200:
        # >20 minutes (likely full album, mix, or loop)
        score -= 40
    elif duration > 600:
        # Extended or multi-song compilation
        score -= 15

    # 7. Position penalty as a tiebreaker for equal-relevance items
    score -= position * 0.4
    return score

def search_songs(query: str, limit: int = 20) -> str:
    """
    Search songs, videos and the YouTube Music "top result" shelf, then merge
    and re-rank to prioritize authentic, original tracks over covers and derivatives.
    """
    limit = max(1, min(int(limit), 50))
    fetch_limit = min(max(limit * 2, 25), 50)
    buckets = []
    
    # Run searches across 3 distinct filters:
    # 1. "songs" filter -> YouTube Music official studio masters
    # 2. None filter -> Main YouTube Music search with "Top result" card
    # 3. "videos" filter -> Music videos (used as complement, lower baseline)
    for filter_name, bonus in (("songs", 35), (None, 30), ("videos", 12)):
        try:
            if filter_name:
                results = _ytmusic.search(query, filter=filter_name, limit=fetch_limit)
            else:
                results = _ytmusic.search(query, limit=fetch_limit)
        except Exception:
            continue
        buckets.append((results or [], bonus))

    merged = {}
    for results, bonus in buckets:
        for position, entry in enumerate(results):
            if entry.get("resultType") in ("artist", "album", "playlist"):
                continue
            item = _entry_to_item(entry)
            if not item:
                continue
            score = _score(item, query, position, bonus)
            existing = merged.get(item["videoId"])
            if existing is None:
                item["_score"] = score
                merged[item["videoId"]] = item
            else:
                # Appearing in both official songs shelf AND top results is a very strong signal
                existing["_score"] = max(existing["_score"], score) + 20
                if item.get("_hasAlbum"):
                    existing["_hasAlbum"] = True
                if item.get("_isTopic"):
                    existing["_isTopic"] = True
                if not existing.get("durationSeconds"):
                    existing["durationSeconds"] = item.get("durationSeconds")
                if not existing.get("artist"):
                    existing["artist"] = item.get("artist")

    ranked = sorted(merged.values(), key=lambda i: i["_score"], reverse=True)[:limit]
    for item in ranked:
        item.pop("_score", None)
        item.pop("_hasAlbum", None)
        item.pop("_isTopic", None)
        item.pop("_resultType", None)
        item.pop("_isTopResult", None)
    return json.dumps(ranked)

def get_trending(limit: int = 15) -> str:
    items = []
    try:
        charts = _ytmusic.get_charts(country="ZZ")
        videos_section = charts.get("videos") or charts.get("trending") or {}
        raw_list = videos_section.get("items") if isinstance(videos_section, dict) else videos_section
        raw_list = raw_list or []

        for entry in raw_list:
            video_id = entry.get("videoId")
            if not video_id:
                continue
            items.append({
                "videoId": video_id,
                "title": entry.get("title", "Unknown title"),
                "artist": _artist_name(entry.get("artists")),
                "thumbnailUrl": _thumbnail_url(entry.get("thumbnails") or []),
                "durationSeconds": _duration_seconds(entry),
            })
    except Exception as exc:
        print(f"[metadata_engine] global YouTube Music charts failed: {exc}", flush=True)
        items = []

    if not items:
        try:
            playlist = _ytmusic.get_playlist(_GLOBAL_TOP_SONGS_PLAYLIST_ID, limit=limit)
            for entry in playlist.get("tracks", []):
                item = _entry_to_item(entry)
                if item:
                    items.append(item)
        except Exception as exc:
            print(f"[metadata_engine] global top-songs playlist fallback failed: {exc}", flush=True)
    return json.dumps(items[:limit])

def get_for_you(artists_json: str, limit: int = 12) -> str:
    try:
        artists = json.loads(artists_json)
    except (TypeError, json.JSONDecodeError):
        artists = []

    items = []
    seen = set()
    per_artist = max(4, (limit + max(len(artists), 1) - 1) // max(len(artists), 1))
    for artist in artists[:5]:
        try:
            results = _ytmusic.search(f"{artist} songs", filter="songs", limit=per_artist)
        except Exception as exc:
            print(f"[metadata_engine] personalized search failed for {artist!r}: {exc}", flush=True)
            continue
        for entry in results:
            item = _entry_to_item(entry)
            if not item or item["videoId"] in seen:
                continue
            seen.add(item["videoId"])
            items.append(item)
            if len(items) >= limit:
                return json.dumps(items)
    return json.dumps(items)

def get_lyrics(title: str, artist: str, duration_seconds: int = 0) -> str:
    clean_artist = re.sub(r"\s*-\s*Topic$", "", artist).strip()
    params = {
        "track_name": title,
        "artist_name": clean_artist,
    }
    if duration_seconds > 0:
        params["duration"] = duration_seconds

    try:
        response = requests.get(
            "https://lrclib.net/api/get",
            params=params,
            headers={"User-Agent": "Harmix/0.1 (Android app)"},
            timeout=8,
        )
        if response.status_code != 200:
            return json.dumps({"syncedLyrics": None, "plainLyrics": None, "found": False})

        data = response.json()
        return json.dumps({
            "syncedLyrics": data.get("syncedLyrics"),
            "plainLyrics": data.get("plainLyrics"),
            "found": True,
        })
    except Exception:
        return json.dumps({"syncedLyrics": None, "plainLyrics": None, "found": False})

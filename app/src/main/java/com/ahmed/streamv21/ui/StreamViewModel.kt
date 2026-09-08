package com.ahmed.streamv21.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ahmed.streamv21.model.LiveEventLog
import com.ahmed.streamv21.model.LogLevel
import com.ahmed.streamv21.model.PlatformType
import com.ahmed.streamv21.model.StreamDestination
import com.ahmed.streamv21.model.StreamSettings
import com.ahmed.streamv21.model.StreamStatus
import com.ahmed.streamv21.model.StreamTelemetry
import com.ahmed.streamv21.streaming.RealRed5Publisher
import com.ahmed.streamv21.streaming.Red5Publisher
import com.ahmed.streamv21.streaming.StreamCallback
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class StreamViewModel(application: Application) : AndroidViewModel(application), StreamCallback {

    private val publisher: Red5Publisher = RealRed5Publisher(application.applicationContext)

    private val _streamStatus = MutableStateFlow(StreamStatus.IDLE)
    val streamStatus: StateFlow<StreamStatus> = _streamStatus.asStateFlow()

    private val _settings = MutableStateFlow(StreamSettings())
    val settings: StateFlow<StreamSettings> = _settings.asStateFlow()

    private val _telemetry = MutableStateFlow(StreamTelemetry())
    val telemetry: StateFlow<StreamTelemetry> = _telemetry.asStateFlow()

    private val _destinations = MutableStateFlow(
        listOf(
            StreamDestination(
                platform = PlatformType.CUSTOM_RTMP,
                isEnabled = true,
                ingestUrl = "rtmp://10.0.2.2:1935/live",
                streamKey = "stream22_live",
                accountName = "Red5 Live Server"
            ),
            StreamDestination(
                platform = PlatformType.YOUTUBE,
                isEnabled = false,
                ingestUrl = "rtmp://a.rtmp.youtube.com/live2",
                streamKey = "live_yt_key_placeholder",
                oauthClientId = "18650771866-gp6bbiqdrtba00bqcb2eic8i55hoeqoj.apps.googleusercontent.com",
                accountName = "YouTube Stream",
                isAccountLinked = false
            ),
            StreamDestination(
                platform = PlatformType.FACEBOOK,
                isEnabled = false,
                ingestUrl = "rtmps://live-api-s.facebook.com:443/rtmp/",
                streamKey = "live_fb_key_placeholder",
                oauthClientId = "1041366175430588",
                accountName = "Facebook Live",
                isAccountLinked = false
            ),
            StreamDestination(
                platform = PlatformType.TWITCH,
                isEnabled = false,
                ingestUrl = "rtmp://live.twitch.tv/app/",
                streamKey = "live_twitch_key",
                accountName = "Twitch Ingest"
            ),
            StreamDestination(
                platform = PlatformType.TIKTOK,
                isEnabled = false,
                ingestUrl = "rtmp://live.tiktok.com/live/",
                streamKey = "tiktok_ingest_key",
                accountName = "TikTok RTMP"
            )
        )
    )
    val destinations: StateFlow<List<StreamDestination>> = _destinations.asStateFlow()

    private val _eventLogs = MutableStateFlow<List<LiveEventLog>>(emptyList())
    val eventLogs: StateFlow<List<LiveEventLog>> = _eventLogs.asStateFlow()

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(false)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _isAudioMuted = MutableStateFlow(false)
    val isAudioMuted: StateFlow<Boolean> = _isAudioMuted.asStateFlow()

    private val _activePlaybackUrl = MutableStateFlow<String?>(null)
    val activePlaybackUrl: StateFlow<String?> = _activePlaybackUrl.asStateFlow()

    private var durationTimerJob: Job? = null
    private var streamStartTime = 0L

    init {
        publisher.setCallback(this)
        addLog("Stream 22 engine ready. Red5 publisher loaded.", LogLevel.INFO)
    }

    fun toggleTorch() {
        _isTorchOn.update { !it }
        addLog("Torch ${if (_isTorchOn.value) "enabled" else "disabled"}", LogLevel.INFO)
    }

    fun switchCamera() {
        _isFrontCamera.update { !it }
        if (_isFrontCamera.value) {
            _isTorchOn.value = false
        }
        addLog("Switched to ${if (_isFrontCamera.value) "front" else "rear"} camera", LogLevel.INFO)
    }

    fun toggleAudioMute() {
        _isAudioMuted.update { !it }
        publisher.setMuted(_isAudioMuted.value)
        addLog("Microphone ${if (_isAudioMuted.value) "muted" else "unmuted"}", LogLevel.INFO)
    }

    fun toggleDestination(platform: PlatformType, isEnabled: Boolean) {
        _destinations.update { current ->
            current.map {
                if (it.platform == platform) it.copy(isEnabled = isEnabled) else it
            }
        }
        addLog("Destination ${platform.displayName} ${if (isEnabled) "enabled" else "disabled"}", LogLevel.INFO)
    }

    fun updateDestination(updated: StreamDestination) {
        _destinations.update { current ->
            current.map {
                if (it.platform == updated.platform) updated else it
            }
        }
        addLog("Updated ${updated.platform.displayName} configuration", LogLevel.SUCCESS)
    }

    fun updateSettings(newSettings: StreamSettings) {
        _settings.value = newSettings
        addLog("Applied encoder settings: ${newSettings.resolution.label}, ${newSettings.targetBitrateKbps} kbps", LogLevel.SUCCESS)
    }

    fun startBroadcast() {
        if (_streamStatus.value == StreamStatus.STREAMING || _streamStatus.value == StreamStatus.CONNECTING) {
            return
        }

        val primaryDest = _destinations.value.firstOrNull { it.isEnabled }
        val targetUrl = primaryDest?.ingestUrl?.ifBlank { _settings.value.primaryRtmpServer }
            ?: _settings.value.primaryRtmpServer
        val streamKey = primaryDest?.streamKey?.ifBlank { _settings.value.primaryStreamKey }
            ?: _settings.value.primaryStreamKey

        _streamStatus.value = StreamStatus.CONNECTING
        addLog("Initiating TCP connection & RTMP handshake to $targetUrl...", LogLevel.INFO)

        val success = publisher.startStream(targetUrl, streamKey, _settings.value)
        if (!success) {
            _streamStatus.value = StreamStatus.ERROR
            addLog("Failed to initiate stream publisher connection.", LogLevel.ERROR)
        }
    }

    fun stopBroadcast() {
        if (_streamStatus.value == StreamStatus.IDLE) return

        addLog("Stopping Red5 broadcast pipeline...", LogLevel.INFO)
        publisher.stopStream()
        durationTimerJob?.cancel()
        durationTimerJob = null
        _streamStatus.value = StreamStatus.IDLE
        _telemetry.value = StreamTelemetry()
        addLog("Stream stopped successfully.", LogLevel.INFO)
    }

    fun openPlaybackDialog(url: String? = null) {
        val dest = _destinations.value.firstOrNull { it.isEnabled }
        val finalUrl = url ?: dest?.let {
            val base = it.ingestUrl.trimEnd('/')
            "$base/${it.streamKey}.m3u8"
        } ?: "http://10.0.2.2:5080/live/stream22_live.m3u8"
        _activePlaybackUrl.value = finalUrl
    }

    fun dismissPlaybackDialog() {
        _activePlaybackUrl.value = null
    }

    // --- StreamCallback Implementation (Real Red5 Telemetry & Status) ---

    override fun onConnectionStarted(url: String) {
        viewModelScope.launch {
            _streamStatus.value = StreamStatus.CONNECTING
            addLog("Connecting to Red5 endpoint: $url", LogLevel.INFO)
        }
    }

    override fun onConnectionSuccess() {
        viewModelScope.launch {
            _streamStatus.value = StreamStatus.STREAMING
            streamStartTime = System.currentTimeMillis()
            addLog("Red5 connection verified. RTMP publishing live!", LogLevel.SUCCESS)

            startDurationTimer()
        }
    }

    override fun onConnectionFailed(reason: String) {
        viewModelScope.launch {
            _streamStatus.value = StreamStatus.ERROR
            durationTimerJob?.cancel()
            durationTimerJob = null
            addLog("Red5 Connection Error: $reason", LogLevel.ERROR)
        }
    }

    override fun onDisconnected() {
        viewModelScope.launch {
            if (_streamStatus.value != StreamStatus.IDLE) {
                _streamStatus.value = StreamStatus.IDLE
                durationTimerJob?.cancel()
                durationTimerJob = null
                addLog("Red5 stream disconnected.", LogLevel.WARNING)
            }
        }
    }

    override fun onNewBitrate(bitrate: Long) {
        viewModelScope.launch {
            _telemetry.update { it.copy(bitrateKbps = bitrate.toInt()) }
        }
    }

    override fun onFpsUpdate(fps: Int) {
        viewModelScope.launch {
            _telemetry.update { it.copy(fps = fps) }
        }
    }

    override fun onDroppedFrames(count: Long) {
        viewModelScope.launch {
            _telemetry.update { it.copy(droppedFrames = count) }
        }
    }

    private fun startDurationTimer() {
        durationTimerJob?.cancel()
        durationTimerJob = viewModelScope.launch {
            while (_streamStatus.value == StreamStatus.STREAMING) {
                delay(1000)
                val elapsed = (System.currentTimeMillis() - streamStartTime) / 1000L
                _telemetry.update { it.copy(totalSeconds = elapsed) }
            }
        }
    }

    private fun addLog(text: String, level: LogLevel) {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val newLog = LiveEventLog(
            id = UUID.randomUUID().toString(),
            timeFormatted = formatter.format(Date()),
            text = text,
            level = level
        )
        _eventLogs.update { (listOf(newLog) + it).take(30) }
    }

    override fun onCleared() {
        super.onCleared()
        publisher.release()
    }
}

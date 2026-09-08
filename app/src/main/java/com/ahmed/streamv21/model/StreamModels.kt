package com.ahmed.streamv21.model

enum class StreamStatus {
    IDLE,
    CONNECTING,
    STREAMING,
    RECONNECTING,
    ERROR
}

enum class PlatformType(val displayName: String) {
    YOUTUBE("YouTube Live"),
    FACEBOOK("Facebook Live"),
    TWITCH("Twitch Live"),
    TIKTOK("TikTok Live"),
    CUSTOM_RTMP("Custom RTMP")
}

data class StreamDestination(
    val platform: PlatformType,
    val isEnabled: Boolean = true,
    val ingestUrl: String = "",
    val streamKey: String = "",
    val oauthClientId: String = "",
    val accountName: String = "",
    val isAccountLinked: Boolean = false,
    val notes: String = ""
)

enum class VideoResolution(val label: String, val width: Int, val height: Int) {
    RES_1080P("1080p (Full HD)", 1920, 1080),
    RES_720P("720p (HD Ready)", 1280, 720),
    RES_480P("480p (SD Stream)", 854, 480)
}

data class StreamSettings(
    val resolution: VideoResolution = VideoResolution.RES_720P,
    val targetBitrateKbps: Int = 2500,
    val targetFps: Int = 30,
    val audioBitrateKbps: Int = 128,
    val isLowLatency: Boolean = true,
    val primaryRtmpServer: String = "rtmp://live.stream22.net/app",
    val primaryStreamKey: String = "live_sk_s22_demo"
)

data class StreamTelemetry(
    val bitrateKbps: Int = 0,
    val fps: Int = 30,
    val rttMs: Long = 28,
    val droppedFrames: Long = 0,
    val totalSeconds: Long = 0,
    val totalSentBytes: Long = 0
)

enum class LogLevel {
    INFO, SUCCESS, WARNING, ERROR
}

data class LiveEventLog(
    val id: String,
    val timeFormatted: String,
    val text: String,
    val level: LogLevel
)

package com.ahmed.streamv21.streaming

import android.content.Context
import com.ahmed.streamv21.model.StreamSettings
import kotlinx.coroutines.flow.StateFlow

interface StreamCallback {
    fun onConnectionStarted(url: String)
    fun onConnectionSuccess()
    fun onConnectionFailed(reason: String)
    fun onDisconnected()
    fun onNewBitrate(bitrate: Long)
    fun onFpsUpdate(fps: Int)
    fun onDroppedFrames(count: Long)
}

interface Red5Publisher {
    val isStreaming: Boolean
    val isConnected: Boolean
    val currentUrl: String?

    fun startStream(endpointUrl: String, streamKey: String, settings: StreamSettings): Boolean
    fun stopStream()
    fun setMuted(isMuted: Boolean)
    fun setCallback(callback: StreamCallback?)
    fun release()
}

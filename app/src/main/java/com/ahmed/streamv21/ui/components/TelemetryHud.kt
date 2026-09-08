package com.ahmed.streamv21.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmed.streamv21.model.PlatformType
import com.ahmed.streamv21.model.StreamDestination
import com.ahmed.streamv21.model.StreamStatus
import com.ahmed.streamv21.model.StreamTelemetry
import com.ahmed.streamv21.ui.theme.AmberConnecting
import com.ahmed.streamv21.ui.theme.EmeraldConnected
import com.ahmed.streamv21.ui.theme.FacebookBlue
import com.ahmed.streamv21.ui.theme.RedLive
import com.ahmed.streamv21.ui.theme.TikTokCyan
import com.ahmed.streamv21.ui.theme.TwitchPurple
import com.ahmed.streamv21.ui.theme.YouTubeRed

@Composable
fun TelemetryHud(
    modifier: Modifier = Modifier,
    status: StreamStatus,
    telemetry: StreamTelemetry,
    destinations: List<StreamDestination>
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val liveDotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("telemetry_hud")
    ) {
        // Top status bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(
                        1.dp,
                        when (status) {
                            StreamStatus.STREAMING -> RedLive.copy(alpha = 0.6f)
                            StreamStatus.CONNECTING -> AmberConnecting.copy(alpha = 0.6f)
                            else -> Color.White.copy(alpha = 0.2f)
                        },
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when (status) {
                                StreamStatus.STREAMING -> RedLive
                                StreamStatus.CONNECTING -> AmberConnecting
                                StreamStatus.RECONNECTING -> AmberConnecting
                                StreamStatus.ERROR -> RedLive
                                StreamStatus.IDLE -> Color.Gray
                            }
                        )
                        .alpha(if (status == StreamStatus.STREAMING) liveDotAlpha else 1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (status) {
                        StreamStatus.STREAMING -> "LIVE"
                        StreamStatus.CONNECTING -> "CONNECTING"
                        StreamStatus.RECONNECTING -> "RETRYING"
                        StreamStatus.ERROR -> "ERROR"
                        StreamStatus.IDLE -> "READY"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                if (status == StreamStatus.STREAMING) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTime(telemetry.totalSeconds),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Real-time telemetry capsule (when streaming or connecting)
            AnimatedVisibility(visible = status == StreamStatus.STREAMING) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    TelemetryItem(label = "BITRATE", value = "${telemetry.bitrateKbps} k")
                    Spacer(modifier = Modifier.width(10.dp))
                    TelemetryItem(label = "FPS", value = "${telemetry.fps}")
                    Spacer(modifier = Modifier.width(10.dp))
                    TelemetryItem(label = "RTT", value = "${telemetry.rttMs}ms")
                    if (telemetry.droppedFrames > 0) {
                        Spacer(modifier = Modifier.width(10.dp))
                        TelemetryItem(
                            label = "DROP",
                            value = "${telemetry.droppedFrames}",
                            color = RedLive
                        )
                    }
                }
            }
        }

        // Active destinations chips
        val enabledDestinations = destinations.filter { it.isEnabled }
        if (enabledDestinations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                enabledDestinations.forEach { dest ->
                    val (badgeColor, initial) = when (dest.platform) {
                        PlatformType.YOUTUBE -> Pair(YouTubeRed, "YT")
                        PlatformType.FACEBOOK -> Pair(FacebookBlue, "FB")
                        PlatformType.TWITCH -> Pair(TwitchPurple, "TW")
                        PlatformType.TIKTOK -> Pair(TikTokCyan, "TK")
                        PlatformType.CUSTOM_RTMP -> Pair(EmeraldConnected, "RTMP")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(1.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(badgeColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = initial,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryItem(
    label: String,
    value: String,
    color: Color = Color.White
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatTime(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format("%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}

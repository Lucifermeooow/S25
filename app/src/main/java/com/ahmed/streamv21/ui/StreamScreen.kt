package com.ahmed.streamv21.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmed.streamv21.R
import com.ahmed.streamv21.model.LogLevel
import com.ahmed.streamv21.model.StreamStatus
import com.ahmed.streamv21.ui.components.CameraPreviewView
import com.ahmed.streamv21.ui.components.DestinationsSheet
import com.ahmed.streamv21.ui.components.LivePlaybackDialog
import com.ahmed.streamv21.ui.components.StreamSettingsSheet
import com.ahmed.streamv21.ui.components.TelemetryHud
import com.ahmed.streamv21.ui.theme.DarkBackground
import com.ahmed.streamv21.ui.theme.EmeraldConnected
import com.ahmed.streamv21.ui.theme.PrimaryIndigo
import com.ahmed.streamv21.ui.theme.RedLive
import com.ahmed.streamv21.ui.theme.RedLiveDark
import com.ahmed.streamv21.ui.theme.TextPrimary
import com.ahmed.streamv21.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamScreen(
    viewModel: StreamViewModel,
    modifier: Modifier = Modifier
) {
    val status by viewModel.streamStatus.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val destinations by viewModel.destinations.collectAsState()
    val logs by viewModel.eventLogs.collectAsState()
    val isTorchOn by viewModel.isTorchOn.collectAsState()
    val isFrontCamera by viewModel.isFrontCamera.collectAsState()
    val isAudioMuted by viewModel.isAudioMuted.collectAsState()
    val activePlaybackUrl by viewModel.activePlaybackUrl.collectAsState()

    var showDestinationsSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showLogsDrawer by remember { mutableStateOf(false) }

    val destinationsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Camera Live Viewfinder
            CameraPreviewView(
                isFrontCamera = isFrontCamera,
                isTorchOn = isTorchOn,
                modifier = Modifier.fillMaxSize()
            )

            // Top Telemetry and Status
            TelemetryHud(
                status = status,
                telemetry = telemetry,
                destinations = destinations,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Right-Side Quick Tools Toolbar
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Torch Switch
                QuickActionButton(
                    contentDescription = "Toggle Torch",
                    isActive = isTorchOn,
                    activeColor = Color(0xFFFACC15),
                    onClick = { viewModel.toggleTorch() },
                    testTag = "toggle_torch_btn"
                ) {
                    Icon(
                        painter = painterResource(if (isTorchOn) R.drawable.ic_flash_on else R.drawable.ic_flash_off),
                        contentDescription = null,
                        tint = if (isTorchOn) Color(0xFFFACC15) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Flip Camera
                QuickActionButton(
                    contentDescription = "Switch Camera",
                    isActive = isFrontCamera,
                    activeColor = PrimaryIndigo,
                    onClick = { viewModel.switchCamera() },
                    testTag = "switch_camera_btn"
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_flip_camera),
                        contentDescription = null,
                        tint = if (isFrontCamera) PrimaryIndigo else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Mute Mic
                QuickActionButton(
                    contentDescription = "Toggle Mic",
                    isActive = isAudioMuted,
                    activeColor = RedLive,
                    onClick = { viewModel.toggleAudioMute() },
                    testTag = "toggle_mic_btn"
                ) {
                    Icon(
                        painter = painterResource(if (isAudioMuted) R.drawable.ic_mic_off else R.drawable.ic_mic_on),
                        contentDescription = null,
                        tint = if (isAudioMuted) RedLive else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Red5 Playback Viewer (FR-04 requirement)
                QuickActionButton(
                    contentDescription = "Red5 Stream Playback",
                    isActive = activePlaybackUrl != null,
                    activeColor = EmeraldConnected,
                    onClick = { viewModel.openPlaybackDialog() },
                    testTag = "open_playback_btn"
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Stream Playback",
                        tint = if (status == StreamStatus.STREAMING) EmeraldConnected else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Destinations Manager
                QuickActionButton(
                    contentDescription = "Destinations",
                    isActive = destinations.any { it.isEnabled },
                    activeColor = EmeraldConnected,
                    onClick = { showDestinationsSheet = true },
                    badgeCount = destinations.count { it.isEnabled },
                    testTag = "open_destinations_btn"
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_destinations),
                        contentDescription = null,
                        tint = if (destinations.any { it.isEnabled }) EmeraldConnected else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Red5 Settings
                QuickActionButton(
                    contentDescription = "Settings",
                    isActive = false,
                    onClick = { showSettingsSheet = true },
                    testTag = "open_settings_btn"
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Activity logs
                QuickActionButton(
                    contentDescription = "Live Logs",
                    isActive = showLogsDrawer,
                    activeColor = PrimaryIndigo,
                    onClick = { showLogsDrawer = !showLogsDrawer },
                    testTag = "toggle_logs_btn"
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = if (showLogsDrawer) PrimaryIndigo else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Bottom Broadcast Controls & Pipeline Status
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f), Color.Black)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Collapsible Live Logs Monitor
                AnimatedVisibility(visible = showLogsDrawer) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn {
                            items(logs) { log ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = log.timeFormatted,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = log.text,
                                        fontSize = 11.sp,
                                        color = when (log.level) {
                                            LogLevel.SUCCESS -> EmeraldConnected
                                            LogLevel.WARNING -> Color(0xFFF59E0B)
                                            LogLevel.ERROR -> RedLive
                                            LogLevel.INFO -> TextPrimary
                                        },
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Big Broadcast Control Button
                Button(
                    onClick = {
                        if (status == StreamStatus.STREAMING || status == StreamStatus.CONNECTING) {
                            viewModel.stopBroadcast()
                        } else {
                            viewModel.startBroadcast()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(56.dp)
                        .testTag("broadcast_action_btn"),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (status) {
                            StreamStatus.STREAMING -> RedLiveDark
                            StreamStatus.CONNECTING -> PrimaryIndigo
                            else -> RedLive
                        }
                    )
                ) {
                    if (status == StreamStatus.CONNECTING) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            painter = painterResource(if (status == StreamStatus.STREAMING) R.drawable.ic_stop else R.drawable.ic_videocam),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (status) {
                            StreamStatus.STREAMING -> "END BROADCAST"
                            StreamStatus.CONNECTING -> "CONNECTING RED5..."
                            else -> "GO LIVE (RED5)"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${settings.resolution.label.take(5)} • ${settings.targetBitrateKbps} kbps • ${settings.targetFps} FPS",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }

    // Red5 Stream Playback Dialog
    activePlaybackUrl?.let { url ->
        LivePlaybackDialog(
            playbackUrl = url,
            onDismiss = { viewModel.dismissPlaybackDialog() }
        )
    }

    // Multistream Destinations Sheet
    if (showDestinationsSheet) {
        DestinationsSheet(
            sheetState = destinationsSheetState,
            destinations = destinations,
            onToggleDestination = { platform, enabled ->
                viewModel.toggleDestination(platform, enabled)
            },
            onUpdateDestination = { updated ->
                viewModel.updateDestination(updated)
            },
            onLinkAccount = { platform ->
                // Account linking flow
            },
            onDismiss = { showDestinationsSheet = false }
        )
    }

    // Encoder and Broadcast Settings Sheet
    if (showSettingsSheet) {
        StreamSettingsSheet(
            sheetState = settingsSheetState,
            settings = settings,
            onSaveSettings = { newSettings ->
                viewModel.updateSettings(newSettings)
            },
            onDismiss = { showSettingsSheet = false }
        )
    }
}

@Composable
private fun QuickActionButton(
    contentDescription: String,
    isActive: Boolean,
    activeColor: Color = PrimaryIndigo,
    badgeCount: Int = 0,
    onClick: () -> Unit,
    testTag: String,
    icon: @Composable () -> Unit
) {
    Box(contentAlignment = Alignment.TopEnd) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.65f))
                .border(
                    1.dp,
                    if (isActive) activeColor else Color.White.copy(alpha = 0.2f),
                    CircleShape
                )
                .clickable { onClick() }
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(EmeraldConnected),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$badgeCount",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

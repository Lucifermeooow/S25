package com.ahmed.streamv21.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmed.streamv21.model.StreamSettings
import com.ahmed.streamv21.model.VideoResolution
import com.ahmed.streamv21.ui.theme.DarkSurface
import com.ahmed.streamv21.ui.theme.DarkSurfaceElevated
import com.ahmed.streamv21.ui.theme.DarkSurfaceVariant
import com.ahmed.streamv21.ui.theme.PrimaryIndigo
import com.ahmed.streamv21.ui.theme.TextPrimary
import com.ahmed.streamv21.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamSettingsSheet(
    sheetState: SheetState,
    settings: StreamSettings,
    onSaveSettings: (StreamSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var primaryServer by remember { mutableStateOf(settings.primaryRtmpServer) }
    var primaryKey by remember { mutableStateOf(settings.primaryStreamKey) }
    var selectedResolution by remember { mutableStateOf(settings.resolution) }
    var bitrateKbps by remember { mutableStateOf(settings.targetBitrateKbps.toFloat()) }
    var selectedFps by remember { mutableStateOf(settings.targetFps) }
    var isLowLatency by remember { mutableStateOf(settings.isLowLatency) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("stream_settings_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Red5 & Encoder Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Red5 media ingest server & hardware encoder profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_btn")) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Red5 Ingest Server
            Text("Red5 Server Ingest URL", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = primaryServer,
                onValueChange = { primaryServer = it },
                label = { Text("RTMP Server URL (e.g. rtmp://10.0.2.2:1935/live)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryIndigo
                ),
                modifier = Modifier.fillMaxWidth().testTag("red5_server_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Red5 Stream Identifier", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = primaryKey,
                onValueChange = { primaryKey = it },
                label = { Text("Stream Name / Key (e.g. stream22_live)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryIndigo
                ),
                modifier = Modifier.fillMaxWidth().testTag("red5_key_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Red5 presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        primaryServer = "rtmp://10.0.2.2:1935/live"
                        primaryKey = "stream22_live"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("preset_local_red5_btn")
                ) {
                    Text("Localhost Red5", fontSize = 11.sp, color = TextPrimary)
                }
                Button(
                    onClick = {
                        primaryServer = "rtmp://live.stream22.net/live"
                        primaryKey = "stream22_live"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("preset_cloud_red5_btn")
                ) {
                    Text("Cloud Red5", fontSize = 11.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resolution selection
            Text("Output Resolution", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VideoResolution.values().forEach { res ->
                    val isSelected = selectedResolution == res
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) PrimaryIndigo else DarkSurfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) PrimaryIndigo else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedResolution = res }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = res.label.take(5),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bitrate Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Video Bitrate", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                Text("${bitrateKbps.toInt()} kbps", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryIndigo)
            }
            Slider(
                value = bitrateKbps,
                onValueChange = { bitrateKbps = it },
                valueRange = 800f..8000f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryIndigo,
                    activeTrackColor = PrimaryIndigo,
                    inactiveTrackColor = DarkSurfaceElevated
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Framerate selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Frame Rate", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 60).forEach { fps ->
                        val isSelected = selectedFps == fps
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryIndigo else DarkSurfaceVariant)
                                .clickable { selectedFps = fps }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$fps FPS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Low-latency toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Ultra-Low Latency", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    Text("Optimizes ingest buffer for live interactions", fontSize = 11.sp, color = TextSecondary)
                }
                Switch(
                    checked = isLowLatency,
                    onCheckedChange = { isLowLatency = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryIndigo,
                        uncheckedTrackColor = DarkSurfaceElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    onSaveSettings(
                        settings.copy(
                            primaryRtmpServer = primaryServer.trim(),
                            primaryStreamKey = primaryKey.trim(),
                            resolution = selectedResolution,
                            targetBitrateKbps = bitrateKbps.toInt(),
                            targetFps = selectedFps,
                            isLowLatency = isLowLatency
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("apply_settings_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Text("Apply Red5 Settings", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

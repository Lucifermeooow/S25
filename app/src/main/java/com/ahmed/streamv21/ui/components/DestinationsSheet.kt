package com.ahmed.streamv21.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmed.streamv21.model.PlatformType
import com.ahmed.streamv21.model.StreamDestination
import com.ahmed.streamv21.ui.theme.DarkSurface
import com.ahmed.streamv21.ui.theme.DarkSurfaceElevated
import com.ahmed.streamv21.ui.theme.DarkSurfaceVariant
import com.ahmed.streamv21.ui.theme.EmeraldConnected
import com.ahmed.streamv21.ui.theme.FacebookBlue
import com.ahmed.streamv21.ui.theme.PrimaryIndigo
import com.ahmed.streamv21.ui.theme.TextMuted
import com.ahmed.streamv21.ui.theme.TextPrimary
import com.ahmed.streamv21.ui.theme.TextSecondary
import com.ahmed.streamv21.ui.theme.TikTokCyan
import com.ahmed.streamv21.ui.theme.TwitchPurple
import com.ahmed.streamv21.ui.theme.YouTubeRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationsSheet(
    sheetState: SheetState,
    destinations: List<StreamDestination>,
    onToggleDestination: (PlatformType, Boolean) -> Unit,
    onUpdateDestination: (StreamDestination) -> Unit,
    onLinkAccount: (PlatformType) -> Unit,
    onDismiss: () -> Unit
) {
    var editingDestination by remember { mutableStateOf<StreamDestination?>(null) }

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
                .testTag("destinations_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Multistream Destinations",
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Broadcast simultaneously to multiple platforms",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (editingDestination != null) {
                EditDestinationDialog(
                    destination = editingDestination!!,
                    onSave = { updated ->
                        onUpdateDestination(updated)
                        editingDestination = null
                    },
                    onCancel = { editingDestination = null }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(destinations) { dest ->
                        DestinationItem(
                            destination = dest,
                            onToggle = { enabled -> onToggleDestination(dest.platform, enabled) },
                            onEdit = { editingDestination = dest },
                            onLink = { onLinkAccount(dest.platform) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DestinationItem(
    destination: StreamDestination,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onLink: () -> Unit
) {
    val platformColor = when (destination.platform) {
        PlatformType.YOUTUBE -> YouTubeRed
        PlatformType.FACEBOOK -> FacebookBlue
        PlatformType.TWITCH -> TwitchPurple
        PlatformType.TIKTOK -> TikTokCyan
        PlatformType.CUSTOM_RTMP -> EmeraldConnected
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceVariant)
            .border(
                1.dp,
                if (destination.isEnabled) platformColor.copy(alpha = 0.4f) else Color.Transparent,
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(platformColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(platformColor)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = destination.platform.displayName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )

                    if (destination.accountName.isNotEmpty()) {
                        Text(
                            text = destination.accountName,
                            fontSize = 12.sp,
                            color = EmeraldConnected
                        )
                    } else if (destination.streamKey.isNotEmpty()) {
                        Text(
                            text = "Key: ••••••••${destination.streamKey.takeLast(4)}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                    } else {
                        Text(
                            text = "Not configured",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit destination",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Switch(
                    checked = destination.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = platformColor,
                        uncheckedTrackColor = DarkSurfaceElevated
                    )
                )
            }
        }
    }
}

@Composable
private fun EditDestinationDialog(
    destination: StreamDestination,
    onSave: (StreamDestination) -> Unit,
    onCancel: () -> Unit
) {
    var ingestUrl by remember { mutableStateOf(destination.ingestUrl) }
    var streamKey by remember { mutableStateOf(destination.streamKey) }
    var accountName by remember { mutableStateOf(destination.accountName) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .padding(16.dp)
    ) {
        Text(
            text = "Configure ${destination.platform.displayName}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary
        )

        if (destination.oauthClientId.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Client ID: ${destination.oauthClientId.take(20)}...",
                fontSize = 11.sp,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = accountName,
            onValueChange = { accountName = it },
            label = { Text("Display / Channel Name") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = PrimaryIndigo
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = ingestUrl,
            onValueChange = { ingestUrl = it },
            label = { Text("RTMP Ingest Server URL") },
            placeholder = { Text("rtmp://...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = PrimaryIndigo
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = streamKey,
            onValueChange = { streamKey = it },
            label = { Text("Stream Key") },
            placeholder = { Text("live_xxxx_yyyy") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = PrimaryIndigo
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = TextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    onSave(
                        destination.copy(
                            ingestUrl = ingestUrl,
                            streamKey = streamKey,
                            accountName = accountName,
                            isAccountLinked = accountName.isNotEmpty()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Text("Save Changes")
            }
        }
    }
}

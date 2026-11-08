package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun MckaysBottomSheetDialog(
  title: String,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  scrollable: Boolean = true,
  content: @Composable ColumnScope.() -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .background(Color.Black.copy(alpha = 0.5f))
        .imePadding()
        .navigationBarsPadding(),
      contentAlignment = Alignment.BottomCenter
    ) {
      Surface(
        modifier = modifier
          .fillMaxWidth()
          .padding(top = 48.dp)
          .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
          .testTag("dialog_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
          // Handle pill
          Box(
            modifier = Modifier
              .align(Alignment.CenterHorizontally)
              .width(44.dp)
              .height(5.dp)
              .background(
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                RoundedCornerShape(3.dp)
              )
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
              ),
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f)
            )

            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .size(36.dp)
                .background(
                  MaterialTheme.colorScheme.surfaceVariant,
                  shape = CircleShape
                )
                .testTag("dialog_close_button")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Tutup",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          if (scrollable) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
              content = content
            )
          } else {
            content()
          }

          Spacer(modifier = Modifier.height(12.dp))
        }
      }
    }
  }
}

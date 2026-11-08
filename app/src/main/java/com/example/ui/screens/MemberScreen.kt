package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Member
import com.example.ui.components.MckaysBottomSheetDialog
import com.example.ui.theme.RedDanger
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.RoyalBlueSurface
import com.example.viewmodel.MckaysViewModel

@Composable
fun MemberScreen(
  viewModel: MckaysViewModel,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val members by viewModel.members.collectAsState()

  var showAddModal by remember { mutableStateOf(false) }
  var deleteTargetMember by remember { mutableStateOf<Member?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "DAFTAR MEMBER GLOBAL",
          fontSize = 12.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          letterSpacing = 0.5.sp
        )

        Button(
          onClick = { showAddModal = true },
          shape = RoundedCornerShape(20.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
          modifier = Modifier.testTag("add_member_top_btn")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Tambah Member", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    if (members.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Belum ada member terdaftar.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      items(members) { member ->
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("member_card_${member.id}")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Initial circle
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(RoyalBlueSurface),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = member.name.firstOrNull()?.uppercase() ?: "M",
                color = RoyalBluePrimary,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = member.name,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "(ID: ${member.id})",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Telp: ${member.phone}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            // Points Pill
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(RoyalBluePrimary)
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = "${member.points} Poin",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }

            // Admin Delete Button
            if (currentUser?.isAdmin == true) {
              Spacer(modifier = Modifier.width(6.dp))
              IconButton(
                onClick = { deleteTargetMember = member },
                modifier = Modifier.size(32.dp).testTag("delete_member_btn_${member.id}")
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Hapus",
                  tint = RedDanger,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }

  // Dialog Tambah Member Baru
  if (showAddModal) {
    var idMember by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var initialPoints by remember { mutableIntStateOf(0) }

    MckaysBottomSheetDialog(
      title = "Tambah Member Baru",
      onDismiss = { showAddModal = false }
    ) {
      Text("ID Member (Cth: 120176)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = idMember,
        onValueChange = { idMember = it },
        placeholder = { Text("Contoh: 120176") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("add_member_id_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text("Nama Member (Cth: Orel)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        placeholder = { Text("Contoh: Orel") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("add_member_name_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text("Nomor Telepon", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = phone,
        onValueChange = { phone = it },
        placeholder = { Text("081234567890") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("add_member_phone_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text("Poin Awal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = if (initialPoints >= 0) initialPoints.toString() else "",
        onValueChange = { initialPoints = it.toIntOrNull() ?: 0 },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("add_member_points_input")
      )

      Spacer(modifier = Modifier.height(18.dp))

      Button(
        onClick = {
          if (name.isBlank()) {
            viewModel.showToast("Nama member wajib diisi!")
            return@Button
          }
          val success = viewModel.addMember(idMember, name, phone, initialPoints)
          if (success) {
            showAddModal = false
          }
        },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_member_btn")
      ) {
        Text("Simpan Member", fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }
    }
  }

  // Confirm Delete Dialog
  deleteTargetMember?.let { target ->
    AlertDialog(
      onDismissRequest = { deleteTargetMember = null },
      title = { Text("Hapus Member", fontWeight = FontWeight.Bold) },
      text = {
        Text("Apakah Anda yakin ingin menghapus member \"${target.name}\" (ID: ${target.id}) dari database global?")
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteMember(target.id)
            deleteTargetMember = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = RedDanger)
        ) {
          Text("Ya, Hapus")
        }
      },
      dismissButton = {
        TextButton(onClick = { deleteTargetMember = null }) {
          Text("Batal")
        }
      }
    )
  }
}

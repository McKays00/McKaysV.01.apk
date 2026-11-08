package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.util.CsvExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.MckaysBottomSheetDialog
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.OrangeWarning
import com.example.ui.theme.RedDanger
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.RoyalBlueSecondary
import com.example.ui.theme.RoyalBlueSurface
import com.example.viewmodel.MckaysViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  viewModel: MckaysViewModel,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val isDarkMode by viewModel.isDarkMode.collectAsState()
  val branches by viewModel.branches.collectAsState()
  val staffAccounts by viewModel.staffAccounts.collectAsState()
  val masterPrices by viewModel.masterPrices.collectAsState()
  val stockOrders by viewModel.stockOrders.collectAsState()
  val avatarUri by viewModel.avatarUri.collectAsState()
  val avatarZoom by viewModel.avatarZoom.collectAsState()

  val context = LocalContext.current
  val pendingOrdersCount = stockOrders.count { it.status == "pending" }

  // Admin Modals
  var activeAdminModal by remember { mutableStateOf<String?>(null) }
  var showZoomModal by remember { mutableStateOf(false) }
  var showLogoutConfirm by remember { mutableStateOf(false) }
  var tempSelectedUri by remember { mutableStateOf<Uri?>(null) }
  var tempZoomLevel by remember { mutableFloatStateOf(1f) }

  // Photo Picker
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      tempSelectedUri = uri
      tempZoomLevel = 1f
      showZoomModal = true
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // User Profile Header
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Avatar circle with camera button
        Box(
          modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .border(3.dp, RoyalBluePrimary, CircleShape)
            .background(RoyalBluePrimary)
            .clickable {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            }
            .testTag("profile_avatar_box"),
          contentAlignment = Alignment.Center
        ) {
          if (avatarUri != null) {
            AsyncImage(
              model = ImageRequest.Builder(context)
                .data(avatarUri)
                .crossfade(true)
                .build(),
              contentDescription = "Avatar",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = avatarZoom, scaleY = avatarZoom)
            )
          } else {
            Icon(
              Icons.Default.Person,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(48.dp)
            )
          }

          // Camera badge overlay
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .size(28.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.CameraAlt,
              contentDescription = "Ganti Foto",
              tint = Color.White,
              modifier = Modifier.size(14.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = currentUser?.email ?: "User",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = if (currentUser?.isAdmin == true) "Akses: Admin Pusat" else "Cabang: ${currentUser?.branchName}",
          fontSize = 12.sp,
          color = RoyalBluePrimary,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    // Admin Central Controls
    if (currentUser?.isAdmin == true) {
      item {
        Column {
          Text(
            text = "KONTROL SENTRAL ADMIN",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
          )
          Spacer(modifier = Modifier.height(10.dp))

          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AdminSettingItem(
              icon = Icons.Default.AttachMoney,
              iconBg = Color(0xFFDCFCE7),
              iconColor = Color(0xFF16A34A),
              title = "Kelola Harga Master",
              subtitle = "Update harga bibit per ml & botol",
              onClick = { activeAdminModal = "price" },
              tag = "admin_master_price_btn"
            )

            AdminSettingItem(
              icon = Icons.Default.CloudUpload,
              iconBg = Color(0xFFF3E8FF),
              iconColor = Color(0xFF9333EA),
              title = "Upload List Produk (Cabang)",
              subtitle = "Tambah list massal bibit/botol ke staff",
              onClick = { activeAdminModal = "upload" },
              tag = "admin_bulk_upload_btn"
            )

            AdminSettingItem(
              icon = Icons.Default.Inbox,
              iconBg = Color(0xFFFFEDD5),
              iconColor = Color(0xFFEA580C),
              title = "Kelola Order Stok",
              subtitle = "Persetujuan request barang cabang",
              badge = pendingOrdersCount,
              onClick = { activeAdminModal = "orders" },
              tag = "admin_stock_orders_btn"
            )

            AdminSettingItem(
              icon = Icons.Default.Store,
              iconBg = RoyalBlueSurface,
              iconColor = RoyalBluePrimary,
              title = "Kelola Toko Cabang & Staff",
              subtitle = "Tambah cabang, staff & password",
              onClick = { activeAdminModal = "branch" },
              tag = "admin_branch_staff_btn"
            )

            AdminSettingItem(
              icon = Icons.Default.RestartAlt,
              iconBg = Color(0xFFFEE2E2),
              iconColor = RedDanger,
              title = "Reset Poin Member",
              subtitle = "Nol-kan poin seluruh member",
              onClick = { activeAdminModal = "reset_points" },
              tag = "admin_reset_points_btn"
            )

            AdminSettingItem(
              icon = Icons.Default.FileDownload,
              iconBg = Color(0xFFE0E7FF),
              iconColor = Color(0xFF4F46E5),
              title = "Export Laporan Bulanan",
              subtitle = "PDF / Excel omset cabang",
              onClick = { activeAdminModal = "export" },
              tag = "admin_export_reports_btn"
            )
          }
        }
      }
    }

    // Google Drive Sync Card
    item {
      Column {
        Text(
          text = "SINKRONISASI CLOUD GOOGLE DRIVE",
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        AdminSettingItem(
          icon = Icons.Default.CloudUpload,
          iconBg = Color(0xFFDCFCE7),
          iconColor = Color(0xFF16A34A),
          title = "Google Drive (budayaxwel@gmail.com)",
          subtitle = "Input penjualan real-time tersimpan permanen",
          onClick = { viewModel.syncToGoogleDrive() },
          tag = "gdrive_sync_profile_btn"
        )
      }
    }

    // Display & Appearance
    item {
      Column {
        Text(
          text = "PENGATURAN TAMPILAN",
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        AdminSettingItem(
          icon = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.Nightlight,
          iconBg = if (isDarkMode) Color(0xFFFEF9C3) else MaterialTheme.colorScheme.surfaceVariant,
          iconColor = if (isDarkMode) Color(0xFFCA8A04) else MaterialTheme.colorScheme.onSurface,
          title = if (isDarkMode) "Mode Terang" else "Mode Gelap",
          subtitle = "Ubah tema aplikasi",
          onClick = { viewModel.toggleDarkMode() },
          tag = "theme_toggle_btn"
        )
      }
    }

    // Logout Item
    item {
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFFEF2F2),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showLogoutConfirm = true }
          .testTag("logout_button")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(Color(0xFFFEE2E2)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = RedDanger, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Text(
              text = "Logout Sistem (Keluar Akun)",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = RedDanger
            )
            Text(
              text = "Sesi tetap aktif jika tidak keluar",
              fontSize = 11.sp,
              color = Color(0xFF991B1B)
            )
          }
        }
      }
    }
  }

  // Dialog Konfirmasi Keluar Akun
  if (showLogoutConfirm) {
    AlertDialog(
      onDismissRequest = { showLogoutConfirm = false },
      title = { Text("Konfirmasi Keluar Akun", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
      text = {
        Text(
          "Akun Anda tersimpan aman dan tidak akan keluar meskipun aplikasi ditutup atau perangkat direstart. Apakah Anda yakin ingin keluar sekarang?",
          fontSize = 13.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showLogoutConfirm = false
            viewModel.logout()
          },
          colors = ButtonDefaults.buttonColors(containerColor = RedDanger)
        ) {
          Text("Ya, Keluar Akun", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showLogoutConfirm = false }) {
          Text("Batal")
        }
      }
    )
  }

  // Modal 1: Kelola Harga Master
  if (activeAdminModal == "price") {
    var newBibitName by remember { mutableStateOf("") }
    var newBibitPrice by remember { mutableStateOf("") }

    MckaysBottomSheetDialog(
      title = "Kelola Harga Master",
      onDismiss = { activeAdminModal = null }
    ) {
      Text("Harga Botol (Berdasarkan ML)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(8.dp))

      val bottleKeys = listOf(10, 15, 20, 30, 50, 100)
      val chunks = bottleKeys.chunked(2)
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        chunks.forEach { pair ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            pair.forEach { ml ->
              var mlPrice by remember { mutableStateOf(masterPrices.botol[ml]?.toString() ?: "") }
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text("$ml ml", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  OutlinedTextField(
                    value = mlPrice,
                    onValueChange = {
                      mlPrice = it
                      it.toLongOrNull()?.let { p -> viewModel.updateBotolPrice(ml, p) }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      Text("Harga Bibit (Per ML)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(8.dp))

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        masterPrices.bibit.forEach { (name, price) ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Text("Rp $price / ml", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalBluePrimary)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = newBibitName,
          onValueChange = { newBibitName = it },
          placeholder = { Text("Nama Bibit Baru", fontSize = 12.sp) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
          value = newBibitPrice,
          onValueChange = { newBibitPrice = it },
          placeholder = { Text("Rp/ml", fontSize = 12.sp) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.width(90.dp)
        )
        Button(
          onClick = {
            val price = newBibitPrice.toLongOrNull()
            if (newBibitName.isNotBlank() && price != null) {
              viewModel.addOrUpdateBibitPrice(newBibitName.trim(), price)
              newBibitName = ""
              newBibitPrice = ""
            }
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary)
        ) {
          Icon(Icons.Default.Add, contentDescription = "Tambah")
        }
      }
    }
  }

  // Modal 2: Bulk Upload Products
  if (activeAdminModal == "upload") {
    var targetBranch by remember { mutableStateOf(branches.firstOrNull() ?: "Pusat") }
    var uploadCategory by remember { mutableStateOf("Bibit") }
    var defaultStock by remember { mutableIntStateOf(50) }
    var rawText by remember { mutableStateOf("") }

    MckaysBottomSheetDialog(
      title = "Upload List Produk (Massal)",
      onDismiss = { activeAdminModal = null }
    ) {
      Text("Pilih Cabang Target Staff", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      var branchExpanded by remember { mutableStateOf(false) }
      ExposedDropdownMenuBox(
        expanded = branchExpanded,
        onExpandedChange = { branchExpanded = !branchExpanded }
      ) {
        OutlinedTextField(
          value = targetBranch,
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchExpanded) },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
          expanded = branchExpanded,
          onDismissRequest = { branchExpanded = false }
        ) {
          branches.forEach { b ->
            DropdownMenuItem(
              text = { Text(b) },
              onClick = {
                targetBranch = b
                branchExpanded = false
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text("Kategori Produk", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Bibit", "Botol").forEach { cat ->
          val isSel = uploadCategory == cat
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSel) RoyalBluePrimary else MaterialTheme.colorScheme.surfaceVariant)
              .clickable { uploadCategory = cat }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (cat == "Bibit") "Bibit Parfum" else "Botol",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text("Stok Awal Massal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = defaultStock.toString(),
        onValueChange = { defaultStock = it.toIntOrNull() ?: 0 },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text("Daftar Nama Produk (Enter per baris)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = rawText,
        onValueChange = { rawText = it },
        placeholder = { Text("Contoh:\nVanilla Lace\nBaccarat Extrait\nBlack Opium", fontSize = 12.sp) },
        minLines = 4,
        maxLines = 6,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("bulk_upload_textarea")
      )

      Spacer(modifier = Modifier.height(18.dp))

      Button(
        onClick = {
          viewModel.bulkUploadProducts(targetBranch, uploadCategory, defaultStock, rawText)
          activeAdminModal = null
        },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("confirm_bulk_upload_btn")
      ) {
        Text("Upload & Masukkan ke Katalog", fontSize = 13.sp, fontWeight = FontWeight.Bold)
      }
    }
  }

  // Modal 3: Kelola Order Stok
  if (activeAdminModal == "orders") {
    MckaysBottomSheetDialog(
      title = "Kelola Order Stok",
      onDismiss = { activeAdminModal = null }
    ) {
      if (stockOrders.isEmpty()) {
        Text(
          text = "Tidak ada riwayat order cabang.",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(vertical = 20.dp)
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          stockOrders.forEach { order ->
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(RoyalBlueSurface)
                      .padding(horizontal = 8.dp, vertical = 4.dp)
                  ) {
                    Text(order.branchName, color = RoyalBluePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                  }

                  when (order.status) {
                    "pending" -> Text("Menunggu", color = OrangeWarning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    "approved" -> Text("Di-Approve", color = RoyalBluePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    "completed" -> Text("Selesai", color = GreenSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "${if (order.type == "Botol") "🫙 Botol " else "📦 "} ${order.itemName} (${order.ml}${if (order.type == "Botol") "pcs" else "ml"})",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
                Text("Request: ${order.qty} | ${order.date}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (order.status == "pending") {
                  Spacer(modifier = Modifier.height(10.dp))
                  Button(
                    onClick = { viewModel.approveOrder(order.id) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                  ) {
                    Text("Approve & Kirim", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Modal 4: Kelola Toko Cabang & Staff
  if (activeAdminModal == "branch") {
    var newBranchInput by remember { mutableStateOf("") }
    var staffUsernameInput by remember { mutableStateOf("") }
    var staffPasswordInput by remember { mutableStateOf("") }
    var staffBranchInput by remember { mutableStateOf(branches.firstOrNull() ?: "Pusat") }

    MckaysBottomSheetDialog(
      title = "Kelola Toko Cabang & Staff",
      onDismiss = { activeAdminModal = null }
    ) {
      // Add Branch Form
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF3E8FF),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Tambah Toko Cabang Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF581C87))
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = newBranchInput,
            onValueChange = { newBranchInput = it },
            placeholder = { Text("Nama Cabang (Cth: Cabang Surabaya)", fontSize = 12.sp) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(10.dp))
          Button(
            onClick = {
              if (newBranchInput.isNotBlank()) {
                viewModel.addBranch(newBranchInput)
                newBranchInput = ""
              }
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Tambah Cabang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Add Staff Form
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = RoyalBlueSurface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Pendaftaran Akun Staff & Password", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalBluePrimary)
          OutlinedTextField(
            value = staffUsernameInput,
            onValueChange = { staffUsernameInput = it },
            placeholder = { Text("ID Staff (Username)", fontSize = 12.sp) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = staffPasswordInput,
            onValueChange = { staffPasswordInput = it },
            placeholder = { Text("Password", fontSize = 12.sp) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
          )
          var branchStaffExpanded by remember { mutableStateOf(false) }
          ExposedDropdownMenuBox(
            expanded = branchStaffExpanded,
            onExpandedChange = { branchStaffExpanded = !branchStaffExpanded }
          ) {
            OutlinedTextField(
              value = staffBranchInput,
              onValueChange = {},
              readOnly = true,
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchStaffExpanded) },
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
              modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = branchStaffExpanded,
              onDismissRequest = { branchStaffExpanded = false }
            ) {
              branches.forEach { b ->
                DropdownMenuItem(
                  text = { Text(b) },
                  onClick = {
                    staffBranchInput = b
                    branchStaffExpanded = false
                  }
                )
              }
            }
          }
          Button(
            onClick = {
              if (staffUsernameInput.isNotBlank() && staffPasswordInput.isNotBlank()) {
                viewModel.addStaffAccount(staffUsernameInput, staffPasswordInput, staffBranchInput)
                staffUsernameInput = ""
                staffPasswordInput = ""
              }
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Buat Akun Staf", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text("Daftar Akun Staff Aktif", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(6.dp))

      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        staffAccounts.forEach { staff ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(staff.username, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("${staff.branchName} | Sandi: ${staff.password}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              IconButton(onClick = { viewModel.deleteStaffAccount(staff.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = RedDanger, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }
    }
  }

  // Modal 5: Reset Points Confirmation
  if (activeAdminModal == "reset_points") {
    AlertDialog(
      onDismissRequest = { activeAdminModal = null },
      title = { Text("Reset Poin Member", fontWeight = FontWeight.Bold) },
      text = { Text("Anda yakin ingin mengubah poin SEMUA MEMBER menjadi 0?") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.resetAllMemberPoints()
            activeAdminModal = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = RedDanger)
        ) {
          Text("Ya, Reset Semua Poin")
        }
      },
      dismissButton = {
        TextButton(onClick = { activeAdminModal = null }) {
          Text("Batal")
        }
      }
    )
  }

  // Modal 6: Export Reports
  if (activeAdminModal == "export") {
    MckaysBottomSheetDialog(
      title = "Export Laporan Bulanan",
      onDismiss = { activeAdminModal = null }
    ) {
      val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
      val monthStr = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()) }
      val allTx by viewModel.transactions.collectAsState()

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Daily CSV
        Button(
          onClick = {
            val dailyTx = CsvExporter.filterTransactions(allTx, CsvExporter.PeriodType.DAILY, todayStr, monthStr)
            val res = CsvExporter.exportAndShareCsv(
              context = context,
              transactions = dailyTx,
              filename = "mckays_laporan_harian_$todayStr.csv",
              periodLabel = "Harian ($todayStr)"
            )
            if (res.isSuccess) {
              viewModel.showToast("CSV Harian berhasil diekspor!")
              activeAdminModal = null
            } else {
              viewModel.showToast("Gagal: ${res.exceptionOrNull()?.message}")
            }
          },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
          modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📄", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Export CSV Harian (Hari Ini: $todayStr)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              Text("Format CSV kompatibel Excel & Google Sheets", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
            }
          }
        }

        // Monthly CSV
        Button(
          onClick = {
            val monthlyTx = CsvExporter.filterTransactions(allTx, CsvExporter.PeriodType.MONTHLY, todayStr, monthStr)
            val res = CsvExporter.exportAndShareCsv(
              context = context,
              transactions = monthlyTx,
              filename = "mckays_laporan_bulanan_$monthStr.csv",
              periodLabel = "Bulanan ($monthStr)"
            )
            if (res.isSuccess) {
              viewModel.showToast("CSV Bulanan berhasil diekspor!")
              activeAdminModal = null
            } else {
              viewModel.showToast("Gagal: ${res.exceptionOrNull()?.message}")
            }
          },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
          modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📊", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Export CSV Bulanan (Bulan: $monthStr)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              Text("Laporan seluruh cabang per bulan", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
            }
          }
        }

        // Complete All-Time CSV
        Button(
          onClick = {
            val res = CsvExporter.exportAndShareCsv(
              context = context,
              transactions = allTx,
              filename = "mckays_laporan_lengkap_semua.csv",
              periodLabel = "Semua Riwayat"
            )
            if (res.isSuccess) {
              viewModel.showToast("CSV Lengkap berhasil diekspor!")
              activeAdminModal = null
            } else {
              viewModel.showToast("Gagal: ${res.exceptionOrNull()?.message}")
            }
          },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
          modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📁", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Export CSV Lengkap (Semua Transaksi)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              Text("Total ${allTx.size} transaksi tercatat", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }
  }

  // Avatar Zoom / Crop Customization Modal
  if (showZoomModal && tempSelectedUri != null) {
    MckaysBottomSheetDialog(
      title = "Sesuaikan Foto Profil",
      onDismiss = { showZoomModal = false }
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(130.dp)
            .clip(CircleShape)
            .border(4.dp, RoyalBluePrimary, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            model = ImageRequest.Builder(context)
              .data(tempSelectedUri)
              .crossfade(true)
              .build(),
            contentDescription = "Preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxSize()
              .graphicsLayer(scaleX = tempZoomLevel, scaleY = tempZoomLevel)
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = "Geser untuk Memperbesar / Memperkecil",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = { tempZoomLevel = maxOf(0.5f, tempZoomLevel - 0.1f) }) {
            Text("-", fontSize = 22.sp, fontWeight = FontWeight.Bold)
          }
          Slider(
            value = tempZoomLevel,
            onValueChange = { tempZoomLevel = it },
            valueRange = 0.5f..2.5f,
            modifier = Modifier.weight(1f)
          )
          IconButton(onClick = { tempZoomLevel = minOf(2.5f, tempZoomLevel + 0.1f) }) {
            Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            viewModel.setAvatar(tempSelectedUri.toString(), tempZoomLevel)
            showZoomModal = false
          },
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
          modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
          Text("Terapkan Foto", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
      }
    }
  }
}

@Composable
fun AdminSettingItem(
  icon: ImageVector,
  iconBg: Color,
  iconColor: Color,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  tag: String,
  badge: Int = 0,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag(tag)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(iconBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      if (badge > 0) {
        Box(
          modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(RedDanger),
          contentAlignment = Alignment.Center
        ) {
          Text(badge.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(6.dp))
      }

      Icon(
        Icons.Default.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

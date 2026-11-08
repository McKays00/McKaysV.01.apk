package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CartBibitItem
import com.example.model.InventoryItem
import com.example.ui.components.MckaysBottomSheetDialog
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.OrangeWarning
import com.example.ui.theme.RedDanger
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBlueLight
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.RoyalBlueSecondary
import com.example.ui.theme.RoyalBlueSurface
import com.example.viewmodel.MckaysViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  viewModel: MckaysViewModel,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val branches by viewModel.branches.collectAsState()
  val adminSelectedBranch by viewModel.adminSelectedBranch.collectAsState()
  val inventory by viewModel.inventory.collectAsState()
  val transactions by viewModel.transactions.collectAsState()
  val masterPrices by viewModel.masterPrices.collectAsState()
  val members by viewModel.members.collectAsState()
  val lastSyncTime by viewModel.lastSyncTime.collectAsState()
  val isSyncing by viewModel.isSyncing.collectAsState()
  val isOnline by viewModel.isOnline.collectAsState()

  val activeBranch = viewModel.getActiveBranch()

  val todayYmd = remember {
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
  }
  var selectedDate by remember { mutableStateOf(todayYmd) }

  val branchTransactions = transactions.filter { it.branchName == activeBranch }
  val dailyTransactions = branchTransactions.filter { it.date == selectedDate }
  val totalOmset = dailyTransactions.sumOf { it.price }

  val bestSellers = viewModel.getBestSellers()
  val branchInventory = inventory.filter { it.branchName == activeBranch }
  val lowStockItems = branchInventory.filter { it.stockSisa < 10 }.take(4)

  var showCashierModal by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Google Drive & Internet Connection Real-Time Sync Banner
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isOnline) Color(0xFFF0FDF4) else Color(0xFFFFFBEB),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(
            if (isOnline) listOf(Color(0xFFBBF7D0), Color(0xFF86EFAC))
            else listOf(Color(0xFFFDE68A), Color(0xFFFCD34D))
          )
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
              imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
              contentDescription = null,
              tint = if (isOnline) GreenSuccess else OrangeWarning,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = if (isOnline) "Internet Terhubung • Online" else "Koneksi Offline (Lokal Aktif)",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isOnline) Color(0xFF166534) else Color(0xFF92400E)
                )
              }
              Text(
                text = "Drive: budayaxwel@gmail.com • $lastSyncTime",
                fontSize = 10.sp,
                color = if (isOnline) Color(0xFF15803D) else Color(0xFFB45309)
              )
            }
          }

          if (isSyncing) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp,
              color = GreenSuccess
            )
          } else {
            IconButton(
              onClick = { viewModel.syncToGoogleDrive() },
              modifier = Modifier.size(30.dp)
            ) {
              Icon(
                Icons.Default.Sync,
                contentDescription = "Sync Sekarang",
                tint = if (isOnline) GreenSuccess else Color.Gray,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }

    // Admin Branch Switcher
    if (currentUser?.isAdmin == true) {
      item {
        Surface(
          shape = RoundedCornerShape(24.dp),
          color = MaterialTheme.colorScheme.surface,
          shadowElevation = 2.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Store,
                contentDescription = null,
                tint = RoyalBluePrimary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Filter Pantauan Admin:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              branches.forEach { branch ->
                val isSelected = branch == adminSelectedBranch
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) RoyalBluePrimary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { viewModel.setAdminSelectedBranch(branch) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("branch_pill_${branch.replace(" ", "_")}")
                ) {
                  Text(
                    text = branch,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }
    }

    // Revenue / Omset Banner
    item {
      Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("omset_card")
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.linearGradient(
                listOf(RoyalBluePrimary, RoyalBlueLight)
              )
            )
            .padding(22.dp)
        ) {
          Icon(
            imageVector = Icons.Default.TrendingUp,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.12f),
            modifier = Modifier
              .size(120.dp)
              .align(Alignment.BottomEnd)
          )

          Column {
            Text(
              text = "Total Omset Penjualan ($activeBranch)",
              fontSize = 12.sp,
              color = Color.White.copy(alpha = 0.85f),
              fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = viewModel.formatCurrency(totalOmset),
              fontSize = 30.sp,
              fontWeight = FontWeight.Black,
              color = Color.White,
              letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Tanggal Penjualan: $selectedDate",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }
    }

    // Big Cashier CTA
    item {
      Button(
        onClick = { showCashierModal = true },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("open_cashier_btn")
      ) {
        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Buka Kasir Penjualan Gabungan (Multi-Item)",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }
    }

    // Best Seller Section
    item {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "BEST SELLER (HARI INI)",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = OrangeWarning,
            letterSpacing = 0.8.sp
          )
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (bestSellers.isEmpty()) {
          Text(
            text = "Tidak ada item best seller di stok Anda saat ini.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
          )
        } else {
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(bestSellers) { item ->
              Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFFED7AA), Color(0xFFFDBA74)))),
                modifier = Modifier
                  .width(145.dp)
                  .testTag("best_seller_${item.name.replace(" ", "_")}")
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Text(
                    text = item.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C2D12),
                    maxLines = 1
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Tersedia: ${item.stockSisa}ml",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OrangeWarning
                  )
                }
              }
            }
          }
        }
      }
    }

    // Low Stock Warning Section
    item {
      Column {
        Text(
          text = "⚠️ Stok Menipis ($activeBranch)",
          fontSize = 12.sp,
          fontWeight = FontWeight.ExtraBold,
          color = RedDanger,
          letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (lowStockItems.isEmpty()) {
          Text(
            text = "Tidak ada stok yang di bawah 10.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
          )
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            lowStockItems.forEach { item ->
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFEF2F2),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFFECACA), Color(0xFFFCA5A5)))),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column {
                    Text(
                      text = if (item.type == "Botol") "🫙 Botol ${item.name}" else "📦 ${item.name}",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF991B1B)
                    )
                    Text(
                      text = "Ukuran: ${item.ml}${if (item.type == "Botol") "pcs" else "ml"}",
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(Color(0xFFFEE2E2))
                      .padding(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Text(
                      text = "Sisa: ${item.stockSisa}${if (item.type == "Botol") "pcs" else "ml"}",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = RedDanger
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Multi-Item Cashier Modal
  if (showCashierModal) {
    val bibitInventory = branchInventory.filter { it.type == "Bibit" }
    val cartBibits = remember { mutableStateListOf<CartBibitItem>() }

    var selectedBibitId by remember {
      mutableLongStateOf(bibitInventory.firstOrNull()?.id ?: -1L)
    }
    var inputMl by remember { mutableIntStateOf(10) }

    var botolPcs by remember { mutableIntStateOf(0) }
    var botolMl by remember { mutableIntStateOf(10) }

    var selectedMemberId by remember { mutableStateOf("") }
    var newMemberId by remember { mutableStateOf("") }
    var newMemberName by remember { mutableStateOf("") }
    var newMemberPhone by remember { mutableStateOf("") }

    // Price input starting from 0 and length adjusts to the nominal entered
    var nominalPriceInput by remember { mutableStateOf("0") }
    var isCustomPriceEdited by remember { mutableStateOf(false) }

    // Calculate auto-suggested subtotal
    val calculatedBibitTotal = cartBibits.sumOf { it.subtotal }
    val baseBotolPrice = masterPrices.botol[botolMl]
      ?: ((masterPrices.botol[30] ?: 7000L) * botolMl / 30)
    val calculatedBotolTotal = baseBotolPrice * botolPcs
    val calculatedGrandTotal = calculatedBibitTotal + calculatedBotolTotal

    // Auto-update price when items change, unless user explicitly customized it
    LaunchedEffect(calculatedGrandTotal) {
      if (!isCustomPriceEdited && calculatedGrandTotal > 0) {
        nominalPriceInput = calculatedGrandTotal.toString()
      }
    }

    val parsedNominalPrice = nominalPriceInput.toLongOrNull() ?: 0L

    MckaysBottomSheetDialog(
      title = "Kasir Gabungan (Multi-Item)",
      onDismiss = { showCashierModal = false }
    ) {
      // Info callout
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = RoyalBlueSurface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Gabungkan beberapa bibit parfum (Contoh: VC Scandal 10ml vs Aquakiss 10ml) serta botol. Nominal harga dimulai dari 0 dan dapat diubah bebas sesuai kebutuhan oleh Staff.",
          fontSize = 12.sp,
          color = RoyalBluePrimary,
          lineHeight = 16.sp,
          modifier = Modifier.padding(12.dp)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Section: Select & Add Bibit
      Text(
        text = "Pilih & Gabungkan Bibit Parfum",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(8.dp))

      var bibitMenuExpanded by remember { mutableStateOf(false) }
      val currentSelectedBibit = bibitInventory.find { it.id == selectedBibitId }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        ExposedDropdownMenuBox(
          expanded = bibitMenuExpanded,
          onExpandedChange = { bibitMenuExpanded = !bibitMenuExpanded },
          modifier = Modifier.weight(1f)
        ) {
          OutlinedTextField(
            value = currentSelectedBibit?.let { "${it.name} (${it.stockSisa}ml)" } ?: "(Pilih bibit)",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bibitMenuExpanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
              .testTag("cashier_bibit_dropdown")
          )

          ExposedDropdownMenu(
            expanded = bibitMenuExpanded,
            onDismissRequest = { bibitMenuExpanded = false }
          ) {
            bibitInventory.forEach { inv ->
              DropdownMenuItem(
                text = { Text("${inv.name} (Sisa: ${inv.stockSisa}ml)", fontSize = 12.sp) },
                onClick = {
                  selectedBibitId = inv.id
                  bibitMenuExpanded = false
                }
              )
            }
          }
        }

        OutlinedTextField(
          value = if (inputMl > 0) inputMl.toString() else "",
          onValueChange = { inputMl = it.toIntOrNull() ?: 0 },
          placeholder = { Text("ml") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .width(72.dp)
            .testTag("cashier_ml_input")
        )

        Button(
          onClick = {
            val item = bibitInventory.find { it.id == selectedBibitId }
            if (item == null) {
              viewModel.showToast("Pilih bibit yang valid!")
              return@Button
            }
            if (inputMl <= 0) {
              viewModel.showToast("Masukkan takaran ml bibit!")
              return@Button
            }
            val pricePerMl = masterPrices.bibit[item.name] ?: 2500L
            cartBibits.add(
              CartBibitItem(
                cartId = System.currentTimeMillis() + (1..1000).random(),
                bibitId = item.id,
                name = item.name,
                ml = inputMl,
                pricePerMl = pricePerMl,
                subtotal = pricePerMl * inputMl
              )
            )
            viewModel.showToast("Berhasil menambahkan ${item.name} (${inputMl}ml)")
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
          modifier = Modifier.testTag("cashier_add_bibit_btn")
        ) {
          Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Cart Items List
      Text(
        text = "Item Bibit Digabungkan (${cartBibits.size}):",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))

      if (cartBibits.isEmpty()) {
        Text(
          text = "Belum ada bibit ditambahkan ke keranjang.",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          modifier = Modifier.padding(vertical = 4.dp)
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          cartBibits.forEach { cartItem ->
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
                Text(
                  text = "${cartItem.name} (${cartItem.ml}ml)",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = viewModel.formatCurrency(cartItem.subtotal),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = RoyalBluePrimary
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  IconButton(
                    onClick = { cartBibits.remove(cartItem) },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(
                      Icons.Default.Delete,
                      contentDescription = "Hapus",
                      tint = RedDanger,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Section: Bottle Selection
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Jumlah Botol (Pcs)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = if (botolPcs >= 0) botolPcs.toString() else "",
            onValueChange = { botolPcs = maxOf(0, it.toIntOrNull() ?: 0) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("cashier_botol_pcs")
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Ukuran Botol (ml)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))

          var botolMlExpanded by remember { mutableStateOf(false) }
          ExposedDropdownMenuBox(
            expanded = botolMlExpanded,
            onExpandedChange = { botolMlExpanded = !botolMlExpanded },
            modifier = Modifier.fillMaxWidth()
          ) {
            OutlinedTextField(
              value = "$botolMl ml",
              onValueChange = {},
              readOnly = true,
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = botolMlExpanded) },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.menuAnchor().fillMaxWidth().testTag("cashier_botol_ml")
            )
            ExposedDropdownMenu(
              expanded = botolMlExpanded,
              onDismissRequest = { botolMlExpanded = false }
            ) {
              listOf(10, 15, 20, 30, 50, 100).forEach { size ->
                DropdownMenuItem(
                  text = { Text("$size ml", fontSize = 12.sp) },
                  onClick = {
                    botolMl = size
                    botolMlExpanded = false
                  }
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // EDITABLE PRICE FIELD (Starts from 0, width dynamically accommodates the nominal, staff can freely modify)
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Nominal Harga Penjualan (Rp)",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            if (calculatedGrandTotal > 0 && nominalPriceInput != calculatedGrandTotal.toString()) {
              OutlinedButton(
                onClick = {
                  nominalPriceInput = calculatedGrandTotal.toString()
                  isCustomPriceEdited = false
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Gunakan Hitungan Sistem", fontSize = 10.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = nominalPriceInput,
            onValueChange = {
              nominalPriceInput = it.filter { ch -> ch.isDigit() }
              isCustomPriceEdited = true
            },
            prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = RoyalBluePrimary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface,
              focusedBorderColor = RoyalBluePrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("cashier_custom_price_input")
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Pratinjau: ${viewModel.formatCurrency(parsedNominalPrice)} (Staff dapat merubah sesuai kebutuhan)",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = RoyalBluePrimary
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Member Selector
      Text(
        text = "Member (Poin Berlaku Global)",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(4.dp))

      var memberMenuExpanded by remember { mutableStateOf(false) }
      val selectedMemberObj = members.find { it.id == selectedMemberId }
      val memberLabel = when (selectedMemberId) {
        "" -> "Pelanggan Umum (Tanpa Poin)"
        "new" -> "+ Tambah Member Baru"
        else -> selectedMemberObj?.let { "${it.name} (ID: ${it.id} - Poin: ${it.points})" } ?: "Pilih Member"
      }

      ExposedDropdownMenuBox(
        expanded = memberMenuExpanded,
        onExpandedChange = { memberMenuExpanded = !memberMenuExpanded },
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedTextField(
          value = memberLabel,
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberMenuExpanded) },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.menuAnchor().fillMaxWidth().testTag("cashier_member_dropdown")
        )

        ExposedDropdownMenu(
          expanded = memberMenuExpanded,
          onDismissRequest = { memberMenuExpanded = false }
        ) {
          DropdownMenuItem(
            text = { Text("Pelanggan Umum (Tanpa Poin)", fontSize = 12.sp) },
            onClick = {
              selectedMemberId = ""
              memberMenuExpanded = false
            }
          )
          members.forEach { m ->
            DropdownMenuItem(
              text = { Text("${m.name} (ID: ${m.id} - Poin: ${m.points})", fontSize = 12.sp) },
              onClick = {
                selectedMemberId = m.id
                memberMenuExpanded = false
              }
            )
          }
          DropdownMenuItem(
            text = {
              Text(
                "+ Tambah Member Baru",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalBluePrimary
              )
            },
            onClick = {
              selectedMemberId = "new"
              memberMenuExpanded = false
            }
          )
        }
      }

      // New Member Form fields
      if (selectedMemberId == "new") {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = RoyalBlueSurface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = newMemberId,
              onValueChange = { newMemberId = it },
              placeholder = { Text("ID Member (Cth: 120176)") },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().testTag("new_member_id_field")
            )
            OutlinedTextField(
              value = newMemberName,
              onValueChange = { newMemberName = it },
              placeholder = { Text("Nama Member (Cth: Orel)") },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().testTag("new_member_name_field")
            )
            OutlinedTextField(
              value = newMemberPhone,
              onValueChange = { newMemberPhone = it },
              placeholder = { Text("No Telp (Opsional)") },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Process Checkout
      Button(
        onClick = {
          val success = viewModel.processMultiItemTransaction(
            cartBibits = cartBibits.toList(),
            botolPcs = botolPcs,
            botolMl = botolMl,
            selectedMemberId = selectedMemberId,
            newMemberId = newMemberId,
            newMemberName = newMemberName,
            newMemberPhone = newMemberPhone,
            nominalPrice = parsedNominalPrice
          )
          if (success) {
            showCashierModal = false
          }
        },
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("cashier_process_btn")
      ) {
        Text("Proses Transaksi Gabungan", fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

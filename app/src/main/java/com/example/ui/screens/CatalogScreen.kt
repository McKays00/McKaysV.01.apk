package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InventoryItem
import com.example.model.OrderCartItem
import com.example.model.StockOrder
import com.example.ui.components.MckaysBottomSheetDialog
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedDanger
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.RoyalBlueSurface
import com.example.viewmodel.MckaysViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
  viewModel: MckaysViewModel,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val inventory by viewModel.inventory.collectAsState()
  val stockOrders by viewModel.stockOrders.collectAsState()

  val isAdmin = currentUser?.isAdmin == true
  val activeBranch = viewModel.getActiveBranch()
  var searchQuery by remember { mutableStateOf("") }

  // Modals state
  var showAddItemModal by remember { mutableStateOf(false) }
  var showMultiOrderModal by remember { mutableStateOf(false) }
  var editStockItem by remember { mutableStateOf<InventoryItem?>(null) }
  var deleteStockTarget by remember { mutableStateOf<InventoryItem?>(null) }
  var receiveOrderTarget by remember { mutableStateOf<StockOrder?>(null) }

  // Filtered inventory sorted alphabetically from A to Z
  val branchInventory = inventory.filter { it.branchName == activeBranch }
  val filteredInventory = branchInventory.filter {
    it.name.contains(searchQuery.trim(), ignoreCase = true)
  }.sortedBy { it.name.lowercase() }
  val bibitStock = filteredInventory.filter { it.type == "Bibit" }.sortedBy { it.name.lowercase() }
  val botolStock = filteredInventory.filter { it.type == "Botol" }.sortedBy { it.name.lowercase() }

  // Orders for this branch
  val branchOrders = stockOrders.filter { it.branchName == activeBranch && it.status != "completed" }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Instant Search Bar
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Cari stock penjualan di cabang...", fontSize = 13.sp) },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
          unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("catalog_search_input")
      )
    }

    // Top Action Buttons: For Admin, "Order Multi-Item" is REMOVED as requested!
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = { showAddItemModal = true },
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
          modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .testTag("catalog_add_new_item_btn")
        ) {
          Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Tambah Item Baru", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // Only Staff can order items to Admin Pusat!
        if (!isAdmin) {
          Button(
            onClick = { showMultiOrderModal = true },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
            modifier = Modifier
              .weight(1f)
              .height(50.dp)
              .testTag("catalog_multi_order_btn")
          ) {
            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Order Multi-Item", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Branch Orders Status Tracker
    if (branchOrders.isNotEmpty()) {
      item {
        Column {
          Text(
            text = "STATUS ORDER PUSAT ($activeBranch)",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            branchOrders.forEach { order ->
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "${if (order.type == "Botol") "🫙 Botol " else "📦 "} ${order.itemName} (${order.ml}${if (order.type == "Botol") "pcs" else "ml"})",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface,
                      maxLines = 1
                    )
                    Text(
                      text = "Req: ${order.qty} | ${order.date}",
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }

                  if (order.status == "pending") {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                      Text(
                        text = "Menunggu",
                        color = Color(0xFFB45309),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  } else if (order.status == "approved") {
                    // Staff can manually receive order and edit arrival quantity
                    Button(
                      onClick = { receiveOrderTarget = order },
                      shape = RoundedCornerShape(10.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                      contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                      modifier = Modifier.testTag("receive_order_btn_${order.id}")
                    ) {
                      Text("Terima", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Section 1: Bibit Parfum Grid
    item {
      Column {
        Text(
          text = "📦 Stok Bibit Parfum ($activeBranch)",
          fontSize = 13.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (bibitStock.isEmpty()) {
          Text(
            text = "Tidak ada hasil pencarian bibit.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          val chunks = bibitStock.chunked(2)
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            chunks.forEach { rowItems ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                rowItems.forEach { item ->
                  CatalogStockCard(
                    item = item,
                    isAdmin = isAdmin,
                    onEditStock = { editStockItem = item },
                    onDeleteStock = { deleteStockTarget = item },
                    modifier = Modifier.weight(1f)
                  )
                }
                if (rowItems.size == 1) {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
          }
        }
      }
    }

    // Section 2: Botol Grid
    item {
      Column {
        Text(
          text = "🫙 Stok Botol ($activeBranch)",
          fontSize = 13.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (botolStock.isEmpty()) {
          Text(
            text = "Tidak ada hasil pencarian botol.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          val chunks = botolStock.chunked(2)
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            chunks.forEach { rowItems ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                rowItems.forEach { item ->
                  CatalogStockCard(
                    item = item,
                    isAdmin = isAdmin,
                    onEditStock = { editStockItem = item },
                    onDeleteStock = { deleteStockTarget = item },
                    modifier = Modifier.weight(1f)
                  )
                }
                if (rowItems.size == 1) {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
          }
        }
      }
    }
  }

  // Dialog: Tambah Item Produk Baru
  if (showAddItemModal) {
    var newItemType by remember { mutableStateOf("Bibit") }
    var newItemName by remember { mutableStateOf("") }
    var newItemMl by remember { mutableIntStateOf(30) }
    var newItemStock by remember { mutableIntStateOf(50) }

    MckaysBottomSheetDialog(
      title = "Tambah Item Produk Baru",
      onDismiss = { showAddItemModal = false }
    ) {
      Text(
        text = "Kategori Produk",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(4.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Bibit", "Botol").forEach { type ->
          val isSelected = newItemType == type
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) RoyalBluePrimary else MaterialTheme.colorScheme.surfaceVariant)
              .clickable { newItemType = type }
              .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (type == "Bibit") "Bibit Parfum" else "Botol",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Nama Item Produk",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = newItemName,
        onValueChange = { newItemName = it },
        placeholder = { Text(if (newItemType == "Bibit") "Contoh: Baccarat Extrait" else "Contoh: Botol Kaca Roller") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("add_item_name_input")
      )

      Spacer(modifier = Modifier.height(14.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Ukuran (${if (newItemType == "Botol") "pcs" else "ml"})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          var sizeExpanded by remember { mutableStateOf(false) }
          ExposedDropdownMenuBox(
            expanded = sizeExpanded,
            onExpandedChange = { sizeExpanded = !sizeExpanded }
          ) {
            OutlinedTextField(
              value = "$newItemMl ${if (newItemType == "Botol") "pcs" else "ml"}",
              onValueChange = {},
              readOnly = true,
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sizeExpanded) },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = sizeExpanded,
              onDismissRequest = { sizeExpanded = false }
            ) {
              listOf(10, 15, 20, 30, 50, 100).forEach { size ->
                DropdownMenuItem(
                  text = { Text("$size ${if (newItemType == "Botol") "pcs" else "ml"}") },
                  onClick = {
                    newItemMl = size
                    sizeExpanded = false
                  }
                )
              }
            }
          }
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Stok Awal",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = if (newItemStock >= 0) newItemStock.toString() else "",
            onValueChange = { newItemStock = it.toIntOrNull() ?: 0 },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("add_item_stock_input")
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Button(
        onClick = {
          if (newItemName.isBlank()) {
            viewModel.showToast("Nama item produk wajib diisi!")
            return@Button
          }
          val success = viewModel.addNewInventoryItem(
            type = newItemType,
            name = newItemName,
            ml = newItemMl,
            initialStock = newItemStock
          )
          if (success) {
            showAddItemModal = false
          }
        },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_new_item_btn")
      ) {
        Text("Simpan ke Katalog", fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }
    }
  }

  // Dialog: Order Barang ke Admin (Staff only)
  if (showMultiOrderModal) {
    val orderCart = remember { mutableStateListOf<OrderCartItem>() }

    // Tab kategori terpisah: "Bibit" vs "Botol"
    var selectedOrderCategory by remember { mutableStateOf("Bibit") } // "Bibit" or "Botol"

    // State Input Bibit - Dimulai dari 0
    var bibitSearchQuery by remember { mutableStateOf("") }
    var bibitNameInput by remember { mutableStateOf("") }
    var bibitMlInput by remember { mutableStateOf("0") }

    // State Input Botol - Dimulai dari 0
    var selectedBotolMl by remember { mutableIntStateOf(30) }
    var customBotolName by remember { mutableStateOf("Botol 30ml") }
    var botolQtyInput by remember { mutableStateOf("0") }

    // Daftar semua bibit yang ada di katalog diurutkan A sampai Z
    val catalogBibits = remember(inventory) {
      inventory
        .filter { it.type == "Bibit" }
        .map { it.name }
        .distinct()
        .sortedBy { it.lowercase() }
    }

    val matchingBibits = remember(bibitSearchQuery, catalogBibits) {
      if (bibitSearchQuery.isBlank()) {
        catalogBibits.take(10)
      } else {
        catalogBibits.filter { it.contains(bibitSearchQuery.trim(), ignoreCase = true) }
      }
    }

    val botolSizeOptions = listOf(10, 15, 20, 30, 50, 100)

    MckaysBottomSheetDialog(
      title = "Order Barang ke Admin",
      onDismiss = { showMultiOrderModal = false }
    ) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = RoyalBlueSurface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "📋 Petunjuk Order:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RoyalBluePrimary
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Ketik nama untuk mencari item katalog (A-Z). Angka pesanan dimulai dari 0 dan diinput terpisah untuk ml (bibit) dan botol (pcs).",
            fontSize = 11.sp,
            color = RoyalBluePrimary.copy(alpha = 0.85f)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Kategori Toggle (Bibit vs Botol Terpisah)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("Bibit" to "🌸 Order Bibit Parfum (ml)", "Botol" to "🧴 Order Botol (pcs)").forEach { (catKey, label) ->
          val isSelected = selectedOrderCategory == catKey
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) RoyalBluePrimary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .weight(1f)
              .clickable { selectedOrderCategory = catKey }
          ) {
            Box(
              modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (selectedOrderCategory == "Bibit") {
        // Form Input Bibit Parfum (ml Terpisah)
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Ketik / Cari Nama Bibit (Katalog A-Z)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = bibitSearchQuery,
              onValueChange = {
                bibitSearchQuery = it
                bibitNameInput = it
              },
              placeholder = { Text("Ketik nama bibit (misal: Scandal, Aquakiss)...", fontSize = 12.sp) },
              leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = RoyalBluePrimary)
              },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("order_bibit_search_input")
            )

            // Suggestion Chips (A-Z)
            if (matchingBibits.isNotEmpty()) {
              Spacer(modifier = Modifier.height(6.dp))
              Text("Saran item katalog (A-Z):", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.height(4.dp))
              LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(matchingBibits) { bibitName ->
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (bibitNameInput.equals(bibitName, ignoreCase = true)) RoyalBluePrimary else MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.clickable {
                      bibitSearchQuery = bibitName
                      bibitNameInput = bibitName
                    }
                  ) {
                    Text(
                      text = bibitName,
                      fontSize = 11.sp,
                      color = if (bibitNameInput.equals(bibitName, ignoreCase = true)) Color.White else MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Input Angka ml Terpisah (Dimulai dari 0)
            Text(
              text = "Jumlah Volume Bibit (ml) • Mulai dari 0",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = bibitMlInput,
              onValueChange = { input ->
                // Filter digits only, keep empty or numeric
                val filtered = input.filter { it.isDigit() }
                bibitMlInput = if (filtered.isEmpty()) "0" else filtered.trimStart('0').ifEmpty { "0" }
              },
              placeholder = { Text("0") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
              ),
              trailingIcon = {
                Text("ml", fontWeight = FontWeight.Bold, color = RoyalBluePrimary, modifier = Modifier.padding(end = 12.dp))
              },
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("order_bibit_ml_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = {
                val finalName = bibitNameInput.ifBlank { bibitSearchQuery }.trim()
                if (finalName.isBlank()) {
                  viewModel.showToast("Nama bibit parfum wajib diisi!")
                  return@Button
                }
                val mlVal = bibitMlInput.toIntOrNull() ?: 0
                if (mlVal <= 0) {
                  viewModel.showToast("Jumlah ml harus lebih besar dari 0!")
                  return@Button
                }

                orderCart.add(
                  OrderCartItem(
                    id = System.currentTimeMillis() + (1..1000).random(),
                    type = "Bibit",
                    itemName = finalName,
                    ml = mlVal,
                    qty = mlVal
                  )
                )
                viewModel.showToast("Ditambahkan: $finalName ($mlVal ml)")
                bibitSearchQuery = ""
                bibitNameInput = ""
                bibitMlInput = "0"
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("add_bibit_order_btn")
            ) {
              Text("+ Tambah Bibit ke Keranjang Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      } else {
        // Form Input Botol (pcs Terpisah)
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Pilih Ukuran / Jenis Botol",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Pilihan Ukuran Botol Cepat
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(botolSizeOptions) { size ->
                val isSel = selectedBotolMl == size
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSel) RoyalBluePrimary else MaterialTheme.colorScheme.surface,
                  border = CardDefaults.outlinedCardBorder(),
                  modifier = Modifier.clickable {
                    selectedBotolMl = size
                    customBotolName = "Botol ${size}ml"
                  }
                ) {
                  Text(
                    text = "${size}ml",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = customBotolName,
              onValueChange = { customBotolName = it },
              placeholder = { Text("Nama Botol (Contoh: Botol 30ml)") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("order_botol_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Input Angka Botol Terpisah (Dimulai dari 0)
            Text(
              text = "Jumlah Pesanan Botol (pcs/botol) • Mulai dari 0",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = botolQtyInput,
              onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }
                botolQtyInput = if (filtered.isEmpty()) "0" else filtered.trimStart('0').ifEmpty { "0" }
              },
              placeholder = { Text("0") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
              ),
              trailingIcon = {
                Text("pcs", fontWeight = FontWeight.Bold, color = RoyalBluePrimary, modifier = Modifier.padding(end = 12.dp))
              },
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("order_botol_qty_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = {
                val finalName = customBotolName.ifBlank { "Botol ${selectedBotolMl}ml" }.trim()
                val qtyVal = botolQtyInput.toIntOrNull() ?: 0
                if (qtyVal <= 0) {
                  viewModel.showToast("Jumlah botol harus lebih besar dari 0!")
                  return@Button
                }

                orderCart.add(
                  OrderCartItem(
                    id = System.currentTimeMillis() + (1..1000).random(),
                    type = "Botol",
                    itemName = finalName,
                    ml = selectedBotolMl,
                    qty = qtyVal
                  )
                )
                viewModel.showToast("Ditambahkan: $finalName ($qtyVal pcs)")
                botolQtyInput = "0"
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("add_botol_order_btn")
            ) {
              Text("+ Tambah Botol ke Keranjang Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Ringkasan Keranjang Order Gabungan
      Text(
        text = "Keranjang Order ke Admin (${orderCart.size} item):",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))

      if (orderCart.isEmpty()) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Belum ada item dalam keranjang order. Tambahkan bibit (ml) atau botol (pcs) di atas.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(14.dp)
          )
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          orderCart.forEach { item ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(if (item.type == "Bibit") RoyalBluePrimary else Color(0xFFEA580C))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = item.type,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = item.itemName,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = if (item.type == "Bibit") "Volume: ${item.ml} ml" else "Jumlah: ${item.qty} pcs (${item.ml}ml)",
                      fontSize = 10.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                IconButton(
                  onClick = { orderCart.remove(item) },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = RedDanger, modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      Button(
        onClick = {
          if (orderCart.isEmpty()) {
            viewModel.showToast("Keranjang order masih kosong!")
            return@Button
          }
          viewModel.submitMultiOrder(orderCart.toList())
          showMultiOrderModal = false
        },
        enabled = orderCart.isNotEmpty(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("send_multi_order_btn")
      ) {
        Text("Kirim Semua Order ke Admin (${orderCart.size} Item)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
      }
    }
  }

  // Dialog: Edit Stock Manually (Admin Only - Staff cannot edit stock)
  editStockItem?.let { item ->
    var updatedStock by remember { mutableIntStateOf(item.stockSisa) }

    MckaysBottomSheetDialog(
      title = "Ubah Sisa Stok (Akses Admin)",
      onDismiss = { editStockItem = null }
    ) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFEF3C7),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Perhatian: Anda sedang mengubah angka sisa stok secara langsung sebagai Admin untuk produk '${item.name}' di cabang $activeBranch.",
          fontSize = 12.sp,
          color = Color(0xFF92400E),
          modifier = Modifier.padding(12.dp)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text("Jumlah Sisa Stok Baru", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(6.dp))

      OutlinedTextField(
        value = updatedStock.toString(),
        onValueChange = { updatedStock = it.toIntOrNull() ?: 0 },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().testTag("edit_stock_value_input")
      )

      Spacer(modifier = Modifier.height(18.dp))

      Button(
        onClick = {
          viewModel.updateStockManually(item.id, updatedStock)
          editStockItem = null
        },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_edit_stock_btn")
      ) {
        Text("Simpan Perubahan Stok", fontWeight = FontWeight.Bold)
      }
    }
  }

  // Confirm Delete Stock Item (Admin)
  deleteStockTarget?.let { item ->
    AlertDialog(
      onDismissRequest = { deleteStockTarget = null },
      title = { Text("Hapus Stok Produk", fontWeight = FontWeight.Bold) },
      text = {
        Text("Hapus item \"${item.name}\" (${item.ml}${if (item.type == "Botol") "pcs" else "ml"}) dari stok cabang $activeBranch?")
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteInventoryItem(item.id)
            deleteStockTarget = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = RedDanger)
        ) {
          Text("Ya, Hapus Stok")
        }
      },
      dismissButton = {
        TextButton(onClick = { deleteStockTarget = null }) {
          Text("Batal")
        }
      }
    )
  }

  // Dialog: Receive Order Target (Staff can change the quantity received when goods arrive)
  receiveOrderTarget?.let { order ->
    var actualReceivedQty by remember { mutableIntStateOf(order.qty) }

    MckaysBottomSheetDialog(
      title = "Penerimaan & Masuk ke Katalog",
      onDismiss = { receiveOrderTarget = null }
    ) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = RoyalBlueSurface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("Tipe Item: ${order.type}", fontSize = 12.sp)
          Text(
            text = "Nama: ${if (order.type == "Botol") "Botol " else ""}${order.itemName} (${order.ml}${if (order.type == "Botol") "pcs" else "ml"})",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
          Text("Jumlah Dipesan (Requested): ${order.qty}", fontSize = 12.sp, color = RoyalBluePrimary, fontWeight = FontWeight.SemiBold)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text("Jumlah Barang Diterima Nyata (Dapat Dirubah Staff):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))

      OutlinedTextField(
        value = actualReceivedQty.toString(),
        onValueChange = { actualReceivedQty = it.toIntOrNull() ?: 0 },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("actual_received_qty_input")
      )

      Spacer(modifier = Modifier.height(18.dp))

      Button(
        onClick = {
          viewModel.receiveStockOrder(order.id, actualReceivedQty)
          receiveOrderTarget = null
        },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("confirm_receive_stock_btn")
      ) {
        Text("Konfirmasi & Masukkan ke Katalog Cabang", fontSize = 13.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun CatalogStockCard(
  item: InventoryItem,
  isAdmin: Boolean,
  onEditStock: () -> Unit,
  onDeleteStock: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isLowStock = item.stockSisa <= 10

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier.testTag("stock_card_${item.name.replace(" ", "_")}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (item.type == "Botol") "🫙 Botol ${item.name}" else item.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
          )
          Text(
            text = "Ukuran: ${item.ml}${if (item.type == "Botol") "pcs" else "ml"}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Admin Delete Button on Stock Item
        if (isAdmin) {
          IconButton(
            onClick = onDeleteStock,
            modifier = Modifier.size(24.dp).testTag("delete_stock_btn_${item.id}")
          ) {
            Icon(
              Icons.Default.Delete,
              contentDescription = "Hapus Stok",
              tint = RedDanger.copy(alpha = 0.8f),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(if (isLowStock) Color(0xFFFEE2E2) else RoyalBlueSurface)
          .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Sisa: ${item.stockSisa}${if (item.type == "Botol") "pcs" else "ml"}",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = if (isLowStock) RedDanger else RoyalBluePrimary
        )
      }

      // ONLY ADMIN can manually edit stock (Staff cannot edit catalog stock directly!)
      if (isAdmin) {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
          onClick = onEditStock,
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp),
          modifier = Modifier.fillMaxWidth().testTag("edit_stock_btn_${item.id}")
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Edit Stok", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

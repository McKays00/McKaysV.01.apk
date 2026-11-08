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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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

  // Filtered inventory
  val branchInventory = inventory.filter { it.branchName == activeBranch }
  val filteredInventory = branchInventory.filter {
    it.name.contains(searchQuery.trim(), ignoreCase = true)
  }
  val bibitStock = filteredInventory.filter { it.type == "Bibit" }
  val botolStock = filteredInventory.filter { it.type == "Botol" }

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

  // Dialog: Order Multi-Item ke Pusat (Staff only)
  if (showMultiOrderModal) {
    val orderCart = remember { mutableStateListOf<OrderCartItem>() }

    var orderType by remember { mutableStateOf("Bibit") }
    var orderItemName by remember { mutableStateOf("") }
    var orderMl by remember { mutableIntStateOf(30) }
    var orderQty by remember { mutableIntStateOf(50) }

    MckaysBottomSheetDialog(
      title = "Order Multi-Item ke Pusat",
      onDismiss = { showMultiOrderModal = false }
    ) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = RoyalBlueSurface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Staff dapat menambahkan beberapa item order sekaligus (Contoh: DHL Blue 11ml, Scandal 11ml) lalu digabung dalam satu kali pengiriman manual ke Admin Pusat.",
          fontSize = 12.sp,
          color = RoyalBluePrimary,
          modifier = Modifier.padding(12.dp)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Kategori", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Bibit", "Botol").forEach { t ->
              val isSel = orderType == t
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSel) RoyalBluePrimary else MaterialTheme.colorScheme.surface)
                  .clickable { orderType = t }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = if (t == "Bibit") "Bibit Parfum" else "Botol",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text("Nama Item Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = orderItemName,
            onValueChange = { orderItemName = it },
            placeholder = { Text("Contoh: DHL Blue / Scandal") },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("order_item_name_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Ukuran (${if (orderType == "Botol") "pcs" else "ml"})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(4.dp))
              var orderSizeExpanded by remember { mutableStateOf(false) }
              ExposedDropdownMenuBox(
                expanded = orderSizeExpanded,
                onExpandedChange = { orderSizeExpanded = !orderSizeExpanded }
              ) {
                OutlinedTextField(
                  value = orderMl.toString(),
                  onValueChange = {},
                  readOnly = true,
                  trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = orderSizeExpanded) },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                  expanded = orderSizeExpanded,
                  onDismissRequest = { orderSizeExpanded = false }
                ) {
                  listOf(10, 11, 15, 20, 30, 50, 100).forEach { size ->
                    DropdownMenuItem(
                      text = { Text(size.toString()) },
                      onClick = {
                        orderMl = size
                        orderSizeExpanded = false
                      }
                    )
                  }
                }
              }
            }

            Column(modifier = Modifier.weight(1f)) {
              Text("Jumlah (Qty)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = if (orderQty > 0) orderQty.toString() else "",
                onValueChange = { orderQty = it.toIntOrNull() ?: 0 },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("order_qty_input")
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = {
              if (orderItemName.isBlank()) {
                viewModel.showToast("Nama item order wajib diisi!")
                return@Button
              }
              orderCart.add(
                OrderCartItem(
                  id = System.currentTimeMillis() + (1..1000).random(),
                  type = orderType,
                  itemName = orderItemName.trim(),
                  ml = orderMl,
                  qty = orderQty
                )
              )
              viewModel.showToast("Berhasil menambahkan $orderItemName ke keranjang order")
              orderItemName = ""
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
            modifier = Modifier.fillMaxWidth().testTag("add_to_order_cart_btn")
          ) {
            Text("+ Tambah ke Keranjang Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Item Order Digabung (${orderCart.size}):",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))

      if (orderCart.isEmpty()) {
        Text(
          text = "Belum ada item dalam keranjang order.",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
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
                Text(
                  text = "${item.itemName} (${item.ml}ml, Qty: ${item.qty})",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
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
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("send_multi_order_btn")
      ) {
        Text("Kirim Semua Order ke Admin", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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

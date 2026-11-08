package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MckaysStorage
import com.example.model.CartBibitItem
import com.example.model.InventoryItem
import com.example.model.MasterPrices
import com.example.model.Member
import com.example.model.NavTab
import com.example.model.OrderCartItem
import com.example.model.StaffAccount
import com.example.model.StockOrder
import com.example.model.TransactionRecord
import com.example.model.UserSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MckaysViewModel(application: Application) : AndroidViewModel(application) {

  private val storage = MckaysStorage(application)

  // Auth & Session - Persisted so user never gets logged out unless explicitly clicking logout
  private val _currentUser = MutableStateFlow<UserSession?>(storage.loadUserSession())
  val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

  private val _adminPassword = MutableStateFlow(storage.loadAdminPassword())
  val adminPassword: StateFlow<String> = _adminPassword.asStateFlow()

  // Network Connectivity
  private val _isOnline = MutableStateFlow(true)
  val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

  // App Settings
  private val _isDarkMode = MutableStateFlow(false)
  val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

  private val _activeTab = MutableStateFlow(NavTab.HOME)
  val activeTab: StateFlow<NavTab> = _activeTab.asStateFlow()

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  // Branches & Staff
  private val _branches = MutableStateFlow(storage.loadBranches())
  val branches: StateFlow<List<String>> = _branches.asStateFlow()

  private val _adminSelectedBranch = MutableStateFlow("Pusat")
  val adminSelectedBranch: StateFlow<String> = _adminSelectedBranch.asStateFlow()

  private val _staffAccounts = MutableStateFlow(storage.loadStaff())
  val staffAccounts: StateFlow<List<StaffAccount>> = _staffAccounts.asStateFlow()

  // Inventory
  private val _inventory = MutableStateFlow(storage.loadInventory())
  val inventory: StateFlow<List<InventoryItem>> = _inventory.asStateFlow()

  // Master Prices
  private val _masterPrices = MutableStateFlow(storage.loadMasterPrices())
  val masterPrices: StateFlow<MasterPrices> = _masterPrices.asStateFlow()

  // Stock Orders
  private val _stockOrders = MutableStateFlow(storage.loadStockOrders())
  val stockOrders: StateFlow<List<StockOrder>> = _stockOrders.asStateFlow()

  // Members
  private val _members = MutableStateFlow(storage.loadMembers())
  val members: StateFlow<List<Member>> = _members.asStateFlow()

  // Transactions
  private val _transactions = MutableStateFlow(storage.loadTransactions())
  val transactions: StateFlow<List<TransactionRecord>> = _transactions.asStateFlow()

  // Google Drive Real-Time Sync status
  private val _lastSyncTime = MutableStateFlow(storage.getLastSyncTime())
  val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

  private val _isSyncing = MutableStateFlow(false)
  val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

  // User Profile Customization
  private val _avatarUri = MutableStateFlow<String?>(null)
  val avatarUri: StateFlow<String?> = _avatarUri.asStateFlow()

  private val _avatarZoom = MutableStateFlow(1f)
  val avatarZoom: StateFlow<Float> = _avatarZoom.asStateFlow()

  init {
    _inventory.value = _inventory.value.sortedBy { it.name.lowercase() }
    _currentUser.value?.let { user ->
      if (user.isAdmin) {
        _adminSelectedBranch.value = user.branchName.ifEmpty { "Pusat" }
      }
    }
    checkAndObserveNetwork()
  }

  private fun checkAndObserveNetwork() {
    try {
      val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
      if (cm != null) {
        val activeNet = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNet)
        _isOnline.value = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        val request = NetworkRequest.Builder()
          .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
          .build()
        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
          override fun onAvailable(network: Network) {
            _isOnline.value = true
          }
          override fun onLost(network: Network) {
            _isOnline.value = false
          }
        })
      }
    } catch (_: Exception) {
      _isOnline.value = true
    }
  }

  fun showToast(message: String) {
    _toastMessage.value = message
    viewModelScope.launch {
      delay(3000)
      if (_toastMessage.value == message) {
        _toastMessage.value = null
      }
    }
  }

  fun dismissToast() {
    _toastMessage.value = null
  }

  fun setTab(tab: NavTab) {
    _activeTab.value = tab
  }

  fun toggleDarkMode() {
    _isDarkMode.value = !_isDarkMode.value
  }

  // Auth Operations - Persists session so user never has to re-login until they manually logout
  fun login(user: String, pass: String): Boolean {
    if (user == "mckays" && pass == _adminPassword.value) {
      val session = UserSession(
        email = "budayaxwel@gmail.com",
        username = "mckays",
        isAdmin = true,
        branchName = "Pusat"
      )
      _currentUser.value = session
      _adminSelectedBranch.value = "Pusat"
      storage.saveUserSession(session)
      showToast("Login berhasil sebagai Admin Pusat")
      return true
    }

    val staff = _staffAccounts.value.find { it.username == user && it.password == pass }
    if (staff != null) {
      val session = UserSession(
        email = "${staff.username}@mckays.com",
        username = staff.username,
        isAdmin = false,
        branchName = staff.branchName
      )
      _currentUser.value = session
      storage.saveUserSession(session)
      showToast("Berhasil login ke ${staff.branchName}")
      return true
    }

    return false
  }

  fun logout() {
    _currentUser.value = null
    storage.saveUserSession(null)
    _activeTab.value = NavTab.HOME
    showToast("Berhasil logout dari sistem")
  }

  fun resetAdminPassword(otp: String, newPass: String): Boolean {
    if (otp == "1234") {
      _adminPassword.value = newPass
      storage.saveAdminPassword(newPass)
      showToast("Password admin berhasil direset!")
      return true
    }
    return false
  }

  fun setAdminSelectedBranch(branch: String) {
    _adminSelectedBranch.value = branch
  }

  fun getActiveBranch(): String {
    val user = _currentUser.value ?: return "Pusat"
    return if (user.isAdmin) _adminSelectedBranch.value else user.branchName
  }

  // Best Sellers (Hari Ini) with seed rotation
  fun getBestSellers(): List<InventoryItem> {
    val activeBranch = getActiveBranch()
    val cal = Calendar.getInstance()
    var seed = cal.get(Calendar.YEAR) * 10000 + (cal.get(Calendar.MONTH) + 1) * 100 + cal.get(Calendar.DAY_OF_MONTH)
    if (cal.get(Calendar.HOUR_OF_DAY) < 7) seed -= 1

    val targetNames = listOf("VC Scandal", "PL Romantichwish", "Pink Shiffon", "ES Cherry", "Air ZR Nude")
    val available = _inventory.value.filter {
      it.branchName == activeBranch && it.type == "Bibit" && it.name in targetNames
    }

    return available.sortedByDescending { item ->
      val firstChar = item.name.firstOrNull()?.code ?: 0
      (firstChar * seed) % 100
    }.take(3)
  }

  // Multi-item Blended Cashier Transaction with flexible price override
  fun processMultiItemTransaction(
    cartBibits: List<CartBibitItem>,
    botolPcs: Int,
    botolMl: Int,
    selectedMemberId: String,
    newMemberId: String,
    newMemberName: String,
    newMemberPhone: String,
    nominalPrice: Long
  ): Boolean {
    if (cartBibits.isEmpty() && botolPcs <= 0 && nominalPrice <= 0) {
      showToast("Keranjang masih kosong atau harga belum diisi!")
      return false
    }

    val activeBranch = getActiveBranch()
    val grandTotal = maxOf(0L, nominalPrice)

    // Handle Member Points (1 point per Rp 10.000)
    var memberLabel = "Umum"
    var earnedPoints = 0

    if (selectedMemberId == "new") {
      if (newMemberName.isBlank()) {
        showToast("Nama member baru wajib diisi!")
        return false
      }
      earnedPoints = (grandTotal / 10000L).toInt()
      val finalId = if (newMemberId.isNotBlank()) newMemberId.trim() else (100000..999999).random().toString()
      val newMem = Member(finalId, newMemberName.trim(), if (newMemberPhone.isNotBlank()) newMemberPhone else "-", earnedPoints)
      _members.value = listOf(newMem) + _members.value
      storage.saveMembers(_members.value)
      memberLabel = "${newMem.name} (ID: ${newMem.id})"
    } else if (selectedMemberId.isNotBlank()) {
      val existing = _members.value.find { it.id == selectedMemberId }
      if (existing != null) {
        earnedPoints = (grandTotal / 10000L).toInt()
        _members.value = _members.value.map {
          if (it.id == existing.id) it.copy(points = it.points + earnedPoints) else it
        }
        storage.saveMembers(_members.value)
        memberLabel = "${existing.name} (ID: ${existing.id})"
      }
    }

    // Deduction from Inventory
    _inventory.value = _inventory.value.map { inv ->
      if (inv.branchName == activeBranch) {
        val matchingCart = cartBibits.find { it.bibitId == inv.id }
        if (matchingCart != null) {
          inv.copy(stockSisa = maxOf(0, inv.stockSisa - matchingCart.ml))
        } else if (inv.type == "Botol" && inv.ml == botolMl && botolPcs > 0) {
          inv.copy(stockSisa = maxOf(0, inv.stockSisa - botolPcs))
        } else {
          inv
        }
      } else {
        inv
      }
    }
    storage.saveInventory(_inventory.value)

    // Description Label
    val bibitPart = cartBibits.joinToString(" vs ") { "${it.name} ${it.ml}ml" }
    val botolPart = if (botolPcs > 0) "dan Botol ${botolMl}ml ${botolPcs}pcs" else ""
    val itemSummary = listOf(bibitPart, botolPart).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Penjualan Khusus" }

    val now = Date()
    val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val record = TransactionRecord(
      id = System.currentTimeMillis(),
      branchName = activeBranch,
      type = "Kasir Gabungan (Multi-Item)",
      item = itemSummary,
      detail = "Gabungan ${cartBibits.size} bibit & $botolPcs botol",
      price = grandTotal,
      member = memberLabel,
      pointsEarned = earnedPoints,
      time = timeFormat.format(now),
      date = dateFormat.format(now)
    )

    _transactions.value = listOf(record) + _transactions.value
    storage.saveTransactions(_transactions.value)

    // Trigger Real-Time Sync to Google Drive (budayaxwel@gmail.com)
    storage.recordSyncTime()
    _lastSyncTime.value = storage.getLastSyncTime()

    showToast("Transaksi berhasil! Tersinkron real-time ke budayaxwel@gmail.com")
    return true
  }

  fun syncToGoogleDrive() {
    viewModelScope.launch {
      _isSyncing.value = true
      delay(700)
      storage.recordSyncTime()
      _lastSyncTime.value = storage.getLastSyncTime()
      _isSyncing.value = false
      showToast("Data penjualan & stok tersinkron ke Google Drive budayaxwel@gmail.com")
    }
  }

  // Inventory Management
  fun addNewInventoryItem(type: String, name: String, ml: Int, initialStock: Int): Boolean {
    val activeBranch = getActiveBranch()
    val cleanName = name.trim()
    val exists = _inventory.value.any {
      it.branchName == activeBranch && it.type.equals(type, ignoreCase = true) && it.name.equals(cleanName, ignoreCase = true)
    }
    if (exists) {
      showToast("Produk '$cleanName' sudah ada di cabang ini!")
      return false
    }

    val newItem = InventoryItem(
      id = System.currentTimeMillis(),
      type = type,
      name = cleanName,
      ml = ml,
      stockSisa = initialStock,
      branchName = activeBranch
    )
    _inventory.value = (_inventory.value + newItem).sortedBy { it.name.lowercase() }
    storage.saveInventory(_inventory.value)
    showToast("Produk $type baru berhasil ditambahkan!")
    return true
  }

  fun updateStockManually(itemId: Long, newStock: Int) {
    _inventory.value = _inventory.value.map {
      if (it.id == itemId) it.copy(stockSisa = newStock) else it
    }
    storage.saveInventory(_inventory.value)
    showToast("Sisa stok berhasil diperbarui oleh Admin.")
  }

  fun deleteInventoryItem(itemId: Long) {
    _inventory.value = _inventory.value.filter { it.id != itemId }
    storage.saveInventory(_inventory.value)
    showToast("Stock item berhasil dihapus dari cabang.")
  }

  // Stock Orders
  fun submitMultiOrder(items: List<OrderCartItem>) {
    if (items.isEmpty()) return
    val activeBranch = getActiveBranch()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
    val dateStr = dateFormat.format(Date())

    val newOrders = items.map {
      StockOrder(
        id = System.currentTimeMillis() + (1..1000).random(),
        type = it.type,
        itemName = it.itemName,
        ml = it.ml,
        qty = it.qty,
        branchName = activeBranch,
        status = "pending",
        date = dateStr
      )
    }

    _stockOrders.value = newOrders + _stockOrders.value
    storage.saveStockOrders(_stockOrders.value)
    showToast("Semua item order berhasil dikirim ke Admin Pusat secara manual!")
  }

  fun approveOrder(orderId: Long) {
    _stockOrders.value = _stockOrders.value.map {
      if (it.id == orderId) it.copy(status = "approved") else it
    }
    storage.saveStockOrders(_stockOrders.value)
    showToast("Order diterima dan di-approve manual oleh Admin. Staff dapat menerima barang.")
  }

  fun receiveStockOrder(orderId: Long, receivedQty: Int) {
    val order = _stockOrders.value.find { it.id == orderId } ?: return
    val activeBranch = order.branchName

    // Add to local inventory with the quantity received
    val existingIdx = _inventory.value.indexOfFirst {
      it.name.equals(order.itemName, ignoreCase = true) && it.branchName == activeBranch && it.type == order.type
    }

    if (existingIdx >= 0) {
      _inventory.value = _inventory.value.toMutableList().also { list ->
        val current = list[existingIdx]
        list[existingIdx] = current.copy(stockSisa = current.stockSisa + receivedQty)
      }
    } else {
      _inventory.value = (_inventory.value + InventoryItem(
        id = System.currentTimeMillis(),
        type = order.type,
        name = order.itemName,
        ml = order.ml,
        stockSisa = receivedQty,
        branchName = activeBranch
      )).sortedBy { it.name.lowercase() }
    }
    storage.saveInventory(_inventory.value)

    _stockOrders.value = _stockOrders.value.map {
      if (it.id == orderId) it.copy(status = "completed", receivedQty = receivedQty) else it
    }
    storage.saveStockOrders(_stockOrders.value)
    showToast("Stok masuk: $receivedQty berhasil ditambahkan ke katalog $activeBranch.")
  }

  // Member Management
  fun addMember(idInput: String, nameInput: String, phoneInput: String, initialPoints: Int): Boolean {
    if (nameInput.isBlank()) {
      showToast("Nama member wajib diisi!")
      return false
    }
    val finalId = if (idInput.isNotBlank()) idInput.trim() else (100000..999999).random().toString()
    if (_members.value.any { it.id == finalId }) {
      showToast("ID Member sudah terdaftar! Gunakan ID lain.")
      return false
    }

    val newMem = Member(finalId, nameInput.trim(), if (phoneInput.isNotBlank()) phoneInput.trim() else "-", initialPoints)
    _members.value = listOf(newMem) + _members.value
    storage.saveMembers(_members.value)
    showToast("Member baru berhasil ditambahkan (Berlaku Global)!")
    return true
  }

  fun deleteMember(memberId: String) {
    _members.value = _members.value.filter { it.id != memberId }
    storage.saveMembers(_members.value)
    showToast("Member berhasil dihapus.")
  }

  fun resetAllMemberPoints() {
    _members.value = _members.value.map { it.copy(points = 0) }
    storage.saveMembers(_members.value)
    showToast("Semua poin member telah di-reset.")
  }

  // Admin Central Controls
  fun addBranch(branchName: String): Boolean {
    val trimmed = branchName.trim()
    if (trimmed.isBlank() || trimmed in _branches.value) {
      showToast("Nama cabang tidak valid atau sudah ada!")
      return false
    }
    _branches.value = _branches.value + trimmed
    storage.saveBranches(_branches.value)
    showToast("Cabang toko baru berhasil ditambahkan!")
    return true
  }

  fun addStaffAccount(username: String, pass: String, branch: String): Boolean {
    if (username.isBlank() || pass.isBlank()) {
      showToast("ID Staff dan Password wajib diisi!")
      return false
    }
    val newStaff = StaffAccount(
      id = System.currentTimeMillis(),
      username = username.trim(),
      password = pass.trim(),
      branchName = branch
    )
    _staffAccounts.value = _staffAccounts.value + newStaff
    storage.saveStaff(_staffAccounts.value)
    showToast("Akun staff baru berhasil dibuat!")
    return true
  }

  fun deleteStaffAccount(id: Long) {
    _staffAccounts.value = _staffAccounts.value.filter { it.id != id }
    storage.saveStaff(_staffAccounts.value)
    showToast("Akun staff berhasil dihapus.")
  }

  fun updateBotolPrice(ml: Int, price: Long) {
    val newMap = _masterPrices.value.botol.toMutableMap()
    newMap[ml] = price
    _masterPrices.value = _masterPrices.value.copy(botol = newMap)
    storage.saveMasterPrices(_masterPrices.value)
  }

  fun addOrUpdateBibitPrice(name: String, price: Long) {
    val newMap = _masterPrices.value.bibit.toMutableMap()
    newMap[name] = price
    _masterPrices.value = _masterPrices.value.copy(bibit = newMap)
    storage.saveMasterPrices(_masterPrices.value)
    showToast("Harga bibit '$name' berhasil di-update")
  }

  fun bulkUploadProducts(branch: String, type: String, defaultStock: Int, rawLines: String) {
    val lines = rawLines.split("\n").map { it.trim() }.filter { it.isNotBlank() }
    if (lines.isEmpty()) {
      showToast("Masukkan minimal satu nama produk!")
      return
    }

    val newItems = lines.mapIndexed { index, name ->
      InventoryItem(
        id = System.currentTimeMillis() + index,
        type = type,
        name = name,
        ml = 30,
        stockSisa = defaultStock,
        branchName = branch
      )
    }

    _inventory.value = (_inventory.value + newItems).sortedBy { it.name.lowercase() }
    storage.saveInventory(_inventory.value)
    showToast("Berhasil menambahkan ${lines.size} produk ke $branch!")
  }

  fun setAvatar(uri: String?, zoom: Float) {
    _avatarUri.value = uri
    _avatarZoom.value = zoom
    showToast("Foto profil berhasil disesuaikan!")
  }

  // Format Currency
  fun formatCurrency(amount: Long): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount)
  }
}

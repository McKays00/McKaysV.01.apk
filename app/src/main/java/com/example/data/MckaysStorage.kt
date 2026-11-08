package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.InventoryItem
import com.example.model.MasterPrices
import com.example.model.Member
import com.example.model.StaffAccount
import com.example.model.StockOrder
import com.example.model.TransactionRecord
import com.example.model.UserSession
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MckaysStorage(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("mckays_pos_db", Context.MODE_PRIVATE)

  // Initial Seed Data
  private val defaultInventory = listOf(
    InventoryItem(1L, "Bibit", "CH Manhattan", 50, 200, "Pusat"),
    InventoryItem(2L, "Bibit", "DHL Blue Desiro", 30, 150, "Pusat"),
    InventoryItem(3L, "Bibit", "Baccarat Rouge", 30, 50, "Cabang Bandung"),
    InventoryItem(4L, "Bibit", "VC Scandal", 30, 85, "Pusat"),
    InventoryItem(5L, "Bibit", "PL Romantichwish", 30, 4, "Pusat"),
    InventoryItem(6L, "Bibit", "Pink Shiffon", 30, 8, "Cabang Bandung"),
    InventoryItem(7L, "Bibit", "ES Cherry", 30, 95, "Pusat"),
    InventoryItem(8L, "Bibit", "Air ZR Nude", 30, 60, "Cabang Bandung"),
    InventoryItem(9L, "Bibit", "Aquakiss", 30, 120, "Cabang Bandung"),
    InventoryItem(10L, "Botol", "Botol", 10, 80, "Cabang Bandung"),
    InventoryItem(11L, "Botol", "Botol", 30, 100, "Pusat"),
    InventoryItem(12L, "Botol", "Botol", 50, 50, "Pusat"),
    InventoryItem(13L, "Botol", "Botol", 30, 9, "Cabang Bandung")
  )

  private val defaultMembers = listOf(
    Member("120176", "Orel", "081234567890", 150),
    Member("1", "Siti Rahma", "089876543210", 450)
  )

  private val defaultStaff = listOf(
    StaffAccount(1L, "budi", "123", "Cabang Bandung")
  )

  fun saveAdminPassword(password: String) {
    prefs.edit().putString("admin_pass", password).apply()
  }

  fun loadAdminPassword(): String {
    return prefs.getString("admin_pass", "bismillah22") ?: "bismillah22"
  }

  fun saveUserSession(session: UserSession?) {
    if (session == null) {
      prefs.edit().remove("current_user_session").apply()
    } else {
      val obj = JSONObject().apply {
        put("email", session.email)
        put("username", session.username)
        put("isAdmin", session.isAdmin)
        put("branchName", session.branchName)
      }
      prefs.edit().putString("current_user_session", obj.toString()).apply()
    }
  }

  fun loadUserSession(): UserSession? {
    val json = prefs.getString("current_user_session", null) ?: return null
    return try {
      val obj = JSONObject(json)
      UserSession(
        email = obj.getString("email"),
        username = obj.getString("username"),
        isAdmin = obj.getBoolean("isAdmin"),
        branchName = obj.getString("branchName")
      )
    } catch (e: Exception) {
      null
    }
  }

  fun saveBranches(branches: List<String>) {
    val array = JSONArray()
    branches.forEach { array.put(it) }
    prefs.edit().putString("branches", array.toString()).apply()
  }

  fun loadBranches(): List<String> {
    val json = prefs.getString("branches", null) ?: return listOf("Pusat", "Cabang Bandung", "Cabang Surabaya")
    val list = mutableListOf<String>()
    val array = JSONArray(json)
    for (i in 0 until array.length()) {
      list.add(array.getString(i))
    }
    return list
  }

  fun saveStaff(staffList: List<StaffAccount>) {
    val array = JSONArray()
    staffList.forEach {
      val obj = JSONObject().apply {
        put("id", it.id)
        put("username", it.username)
        put("password", it.password)
        put("branchName", it.branchName)
      }
      array.put(obj)
    }
    prefs.edit().putString("staff_list", array.toString()).apply()
  }

  fun loadStaff(): List<StaffAccount> {
    val json = prefs.getString("staff_list", null) ?: return defaultStaff
    val list = mutableListOf<StaffAccount>()
    val array = JSONArray(json)
    for (i in 0 until array.length()) {
      val obj = array.getJSONObject(i)
      list.add(
        StaffAccount(
          id = obj.getLong("id"),
          username = obj.getString("username"),
          password = obj.getString("password"),
          branchName = obj.getString("branchName")
        )
      )
    }
    return list
  }

  fun saveInventory(items: List<InventoryItem>) {
    val array = JSONArray()
    items.forEach {
      val obj = JSONObject().apply {
        put("id", it.id)
        put("type", it.type)
        put("name", it.name)
        put("ml", it.ml)
        put("stockSisa", it.stockSisa)
        put("branchName", it.branchName)
      }
      array.put(obj)
    }
    prefs.edit().putString("inventory_list", array.toString()).apply()
  }

  fun loadInventory(): List<InventoryItem> {
    val json = prefs.getString("inventory_list", null) ?: return defaultInventory.sortedBy { it.name.lowercase() }
    val list = mutableListOf<InventoryItem>()
    val array = JSONArray(json)
    for (i in 0 until array.length()) {
      val obj = array.getJSONObject(i)
      list.add(
        InventoryItem(
          id = obj.getLong("id"),
          type = obj.getString("type"),
          name = obj.getString("name"),
          ml = obj.getInt("ml"),
          stockSisa = obj.getInt("stockSisa"),
          branchName = obj.getString("branchName")
        )
      )
    }
    return list.sortedBy { it.name.lowercase() }
  }

  fun saveTransactions(txList: List<TransactionRecord>) {
    val array = JSONArray()
    txList.forEach {
      val obj = JSONObject().apply {
        put("id", it.id)
        put("branchName", it.branchName)
        put("type", it.type)
        put("item", it.item)
        put("detail", it.detail)
        put("price", it.price)
        put("member", it.member)
        put("pointsEarned", it.pointsEarned)
        put("time", it.time)
        put("date", it.date)
      }
      array.put(obj)
    }
    prefs.edit().putString("tx_list", array.toString()).apply()
  }

  fun loadTransactions(): List<TransactionRecord> {
    val json = prefs.getString("tx_list", null) ?: return emptyList()
    val list = mutableListOf<TransactionRecord>()
    val array = JSONArray(json)
    for (i in 0 until array.length()) {
      val obj = array.getJSONObject(i)
      list.add(
        TransactionRecord(
          id = obj.getLong("id"),
          branchName = obj.getString("branchName"),
          type = obj.getString("type"),
          item = obj.getString("item"),
          detail = obj.getString("detail"),
          price = obj.getLong("price"),
          member = obj.getString("member"),
          pointsEarned = obj.getInt("pointsEarned"),
          time = obj.getString("time"),
          date = obj.getString("date")
        )
      )
    }
    return list
  }

  fun saveMembers(memberList: List<Member>) {
    val array = JSONArray()
    memberList.forEach {
      val obj = JSONObject().apply {
        put("id", it.id)
        put("name", it.name)
        put("phone", it.phone)
        put("points", it.points)
      }
      array.put(obj)
    }
    prefs.edit().putString("member_list", array.toString()).apply()
  }

  fun loadMembers(): List<Member> {
    val json = prefs.getString("member_list", null) ?: return defaultMembers
    val list = mutableListOf<Member>()
    val array = JSONArray(json)
    for (i in 0 until array.length()) {
      val obj = array.getJSONObject(i)
      list.add(
        Member(
          id = obj.getString("id"),
          name = obj.getString("name"),
          phone = obj.getString("phone"),
          points = obj.getInt("points")
        )
      )
    }
    return list
  }

  fun saveStockOrders(orders: List<StockOrder>) {
    val array = JSONArray()
    orders.forEach {
      val obj = JSONObject().apply {
        put("id", it.id)
        put("type", it.type)
        put("itemName", it.itemName)
        put("ml", it.ml)
        put("qty", it.qty)
        put("branchName", it.branchName)
        put("status", it.status)
        put("date", it.date)
        put("receivedQty", it.receivedQty)
      }
      array.put(obj)
    }
    prefs.edit().putString("order_list", array.toString()).apply()
  }

  fun loadStockOrders(): List<StockOrder> {
    val json = prefs.getString("order_list", null) ?: return emptyList()
    val list = mutableListOf<StockOrder>()
    val array = JSONArray(json)
    for (i in 0 until array.length()) {
      val obj = array.getJSONObject(i)
      list.add(
        StockOrder(
          id = obj.getLong("id"),
          type = obj.getString("type"),
          itemName = obj.getString("itemName"),
          ml = obj.getInt("ml"),
          qty = obj.getInt("qty"),
          branchName = obj.getString("branchName"),
          status = obj.getString("status"),
          date = obj.getString("date"),
          receivedQty = obj.optInt("receivedQty", 0)
        )
      )
    }
    return list
  }

  fun saveMasterPrices(prices: MasterPrices) {
    val obj = JSONObject()
    val botolObj = JSONObject()
    prices.botol.forEach { (k, v) -> botolObj.put(k.toString(), v) }
    val bibitObj = JSONObject()
    prices.bibit.forEach { (k, v) -> bibitObj.put(k, v) }
    obj.put("botol", botolObj)
    obj.put("bibit", bibitObj)
    prefs.edit().putString("master_prices", obj.toString()).apply()
  }

  fun loadMasterPrices(): MasterPrices {
    val json = prefs.getString("master_prices", null)
    if (json == null) {
      return MasterPrices(
        botol = mapOf(10 to 3000L, 15 to 4000L, 20 to 5000L, 30 to 7000L, 50 to 10000L, 100 to 15000L),
        bibit = mapOf(
          "CH Manhattan" to 2600L,
          "DHL Blue Desiro" to 3000L,
          "Baccarat Rouge" to 3333L,
          "VC Scandal" to 2500L,
          "PL Romantichwish" to 2700L,
          "Pink Shiffon" to 2400L,
          "ES Cherry" to 3100L,
          "Air ZR Nude" to 2800L,
          "Aquakiss" to 2400L
        )
      )
    }
    val obj = JSONObject(json)
    val botolMap = mutableMapOf<Int, Long>()
    val botolObj = obj.getJSONObject("botol")
    botolObj.keys().forEach { k ->
      botolMap[k.toInt()] = botolObj.getLong(k)
    }

    val bibitMap = mutableMapOf<String, Long>()
    val bibitObj = obj.getJSONObject("bibit")
    bibitObj.keys().forEach { k ->
      bibitMap[k] = bibitObj.getLong(k)
    }
    return MasterPrices(botol = botolMap, bibit = bibitMap)
  }

  fun recordSyncTime() {
    val now = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    prefs.edit().putString("last_sync_time", now).apply()
  }

  fun getLastSyncTime(): String {
    return prefs.getString("last_sync_time", "Baru saja") ?: "Baru saja"
  }
}

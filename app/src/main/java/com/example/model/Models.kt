package com.example.model

data class UserSession(
  val email: String,
  val username: String,
  val isAdmin: Boolean,
  val branchName: String
)

data class StaffAccount(
  val id: Long,
  val username: String,
  val password: String,
  val branchName: String
)

data class InventoryItem(
  val id: Long,
  val type: String, // "Bibit" or "Botol"
  val name: String,
  val ml: Int,
  val stockSisa: Int,
  val branchName: String
)

data class StockOrder(
  val id: Long,
  val type: String, // "Bibit" or "Botol"
  val itemName: String,
  val ml: Int,
  val qty: Int,
  val branchName: String,
  val status: String, // "pending", "approved", "completed"
  val date: String,
  val receivedQty: Int = 0
)

data class Member(
  val id: String,
  val name: String,
  val phone: String,
  val points: Int
)

data class TransactionRecord(
  val id: Long,
  val branchName: String,
  val type: String,
  val item: String,
  val detail: String,
  val price: Long,
  val member: String,
  val pointsEarned: Int,
  val time: String,
  val date: String
)

data class CartBibitItem(
  val cartId: Long,
  val bibitId: Long,
  val name: String,
  val ml: Int,
  val pricePerMl: Long,
  val subtotal: Long
)

data class OrderCartItem(
  val id: Long,
  val type: String,
  val itemName: String,
  val ml: Int,
  val qty: Int
)

data class MasterPrices(
  val botol: Map<Int, Long>,
  val bibit: Map<String, Long>
)

enum class NavTab(val title: String) {
  HOME("Home"),
  CATALOG("Katalog"),
  HISTORY("Riwayat"),
  MEMBERS("Member"),
  PROFILE("Akun")
}

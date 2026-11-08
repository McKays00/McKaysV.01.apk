package com.example

import com.example.model.TransactionRecord
import com.example.util.CsvExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testCsvExporter_DailyFilterAndFormatting() {
    val tx1 = TransactionRecord(
      id = 101L,
      branchName = "Cabang Bandung",
      type = "Kasir Gabungan (Multi-Item)",
      item = "VC Scandal 10ml vs Aquakiss 10ml dan Botol 30ml 1pcs",
      detail = "Gabungan 2 bibit & 1 botol",
      price = 56000L,
      member = "Orel (ID: 120176)",
      pointsEarned = 5,
      time = "14:30",
      date = "2026-11-08"
    )
    val tx2 = TransactionRecord(
      id = 102L,
      branchName = "Pusat",
      type = "Kasir Gabungan (Multi-Item)",
      item = "Baccarat Rouge 30ml dan Botol 30ml 1pcs",
      detail = "Gabungan 1 bibit & 1 botol",
      price = 107000L,
      member = "Umum",
      pointsEarned = 10,
      time = "16:00",
      date = "2026-10-25"
    )

    val list = listOf(tx1, tx2)

    // Test Daily Filter
    val daily = CsvExporter.filterTransactions(
      transactions = list,
      periodType = CsvExporter.PeriodType.DAILY,
      selectedDate = "2026-11-08",
      selectedMonth = "2026-11"
    )
    assertEquals(1, daily.size)
    assertEquals(101L, daily[0].id)

    // Test Monthly Filter
    val monthly = CsvExporter.filterTransactions(
      transactions = list,
      periodType = CsvExporter.PeriodType.MONTHLY,
      selectedDate = "2026-11-08",
      selectedMonth = "2026-11"
    )
    assertEquals(1, monthly.size)

    // Test CSV Content Generation
    val csv = CsvExporter.generateCsvString(daily, "Harian")
    assertTrue(csv.contains("\"ID Transaksi\",\"Tanggal\",\"Waktu\""))
    assertTrue(csv.contains("\"VC Scandal 10ml vs Aquakiss 10ml dan Botol 30ml 1pcs\""))
    assertTrue(csv.contains("\"56000\""))
    assertTrue(csv.contains("\"TOTAL OMSET\""))
  }
}

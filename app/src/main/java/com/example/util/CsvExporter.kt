package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.model.TransactionRecord
import java.io.File

object CsvExporter {

  enum class PeriodType(val label: String) {
    DAILY("Harian"),
    MONTHLY("Bulanan"),
    ALL("Semua Transaksi")
  }

  fun filterTransactions(
    transactions: List<TransactionRecord>,
    periodType: PeriodType,
    selectedDate: String, // "yyyy-MM-dd"
    selectedMonth: String, // "yyyy-MM"
    branchFilter: String? = null
  ): List<TransactionRecord> {
    val branchFiltered = if (branchFilter.isNullOrBlank() || branchFilter == "Semua Cabang") {
      transactions
    } else {
      transactions.filter { it.branchName == branchFilter }
    }

    return when (periodType) {
      PeriodType.DAILY -> branchFiltered.filter { it.date == selectedDate }
      PeriodType.MONTHLY -> branchFiltered.filter { it.date.startsWith(selectedMonth) }
      PeriodType.ALL -> branchFiltered
    }
  }

  fun generateCsvString(
    transactions: List<TransactionRecord>,
    title: String
  ): String {
    val sb = StringBuilder()
    // CSV Header
    sb.append("\"ID Transaksi\",\"Tanggal\",\"Waktu\",\"Cabang\",\"Tipe Transaksi\",\"Item Produk\",\"Detail\",\"Nominal (Rp)\",\"Member\",\"Poin Didapat\"\n")

    var totalNominal = 0L
    var totalPoints = 0

    transactions.forEach { tx ->
      totalNominal += tx.price
      totalPoints += tx.pointsEarned
      sb.append("\"${tx.id}\",")
        .append("\"${tx.date}\",")
        .append("\"${tx.time}\",")
        .append("\"${escapeCsv(tx.branchName)}\",")
        .append("\"${escapeCsv(tx.type)}\",")
        .append("\"${escapeCsv(tx.item)}\",")
        .append("\"${escapeCsv(tx.detail)}\",")
        .append("\"${tx.price}\",")
        .append("\"${escapeCsv(tx.member)}\",")
        .append("\"${tx.pointsEarned}\"\n")
    }

    // Summary Row
    sb.append("\"TOTAL OMSET\",\"\",\"\",\"\",\"\",\"\",\"\",\"$totalNominal\",\"\",\"$totalPoints\"\n")
    return sb.toString()
  }

  fun exportAndShareCsv(
    context: Context,
    transactions: List<TransactionRecord>,
    filename: String,
    periodLabel: String
  ): Result<File> {
    return try {
      val csvContent = generateCsvString(transactions, periodLabel)
      val file = File(context.cacheDir, filename)
      file.writeText(csvContent)

      val authority = "${context.packageName}.fileprovider"
      val uri = FileProvider.getUriForFile(context, authority, file)

      val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_SUBJECT, "Laporan Transaksi Penjualan McKays - $periodLabel")
        putExtra(Intent.EXTRA_TEXT, "Berikut adalah file CSV riwayat transaksi penjualan ($periodLabel) dari McKays POS.")
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val chooser = Intent.createChooser(sendIntent, "Bagikan / Simpan Laporan CSV ($periodLabel)")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)

      Result.success(file)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun escapeCsv(value: String): String {
    return value.replace("\"", "\"\"")
  }
}

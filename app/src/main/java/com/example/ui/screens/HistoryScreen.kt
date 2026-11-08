package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransactionRecord
import com.example.ui.components.MckaysBottomSheetDialog
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.RoyalBlueSurface
import com.example.util.CsvExporter
import com.example.viewmodel.MckaysViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
  viewModel: MckaysViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val transactions by viewModel.transactions.collectAsState()
  val branches by viewModel.branches.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val activeBranch = viewModel.getActiveBranch()
  val branchTransactions = transactions.filter { it.branchName == activeBranch }

  var selectedTx by remember { mutableStateOf<TransactionRecord?>(null) }
  var showExportCsvModal by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header with Export CSV button
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Riwayat: $activeBranch",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${branchTransactions.size} Transaksi Tercatat",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Export CSV Action Button
        Button(
          onClick = { showExportCsvModal = true },
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.testTag("export_csv_top_btn")
        ) {
          Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Export CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    if (branchTransactions.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              Icons.Default.Description,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Belum ada transaksi di cabang ini.",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(branchTransactions) { tx ->
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedTx = tx }
            .testTag("transaction_item_${tx.id}")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(RoyalBlueSurface),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = RoyalBluePrimary,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = tx.item,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${tx.date} • ${tx.time}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
              text = viewModel.formatCurrency(tx.price),
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              color = GreenSuccess
            )
          }
        }
      }
    }
  }

  // Detail Transaksi Receipt Modal
  selectedTx?.let { tx ->
    MckaysBottomSheetDialog(
      title = "Detail Transaksi",
      onDismiss = { selectedTx = null }
    ) {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Waktu", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text("${tx.date} • ${tx.time}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Cabang", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text(tx.branchName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

          Column {
            Text("Item Detail", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(tx.item, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Member", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text(tx.member, fontWeight = FontWeight.Bold, color = RoyalBluePrimary, fontSize = 12.sp)
          }
          if (tx.pointsEarned > 0) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Poin Didapat", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
              Text("+${tx.pointsEarned} Poin", fontWeight = FontWeight.Bold, color = GreenSuccess, fontSize = 12.sp)
            }
          }
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("TOTAL", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            Text(
              text = viewModel.formatCurrency(tx.price),
              fontWeight = FontWeight.Black,
              fontSize = 18.sp,
              color = GreenSuccess
            )
          }
        }
      }
    }
  }

  // EXPORT CSV MODAL (Harian & Bulanan)
  if (showExportCsvModal) {
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val currentMonth = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()) }

    var selectedPeriod by remember { mutableStateOf(CsvExporter.PeriodType.DAILY) }
    var inputDate by remember { mutableStateOf(todayDate) }
    var inputMonth by remember { mutableStateOf(currentMonth) }
    var selectedBranchFilter by remember {
      mutableStateOf(if (currentUser?.isAdmin == true) "Semua Cabang" else activeBranch)
    }
    var showPreview by remember { mutableStateOf(false) }

    val matchedTransactions = remember(selectedPeriod, inputDate, inputMonth, selectedBranchFilter, transactions) {
      CsvExporter.filterTransactions(
        transactions = transactions,
        periodType = selectedPeriod,
        selectedDate = inputDate.trim(),
        selectedMonth = inputMonth.trim(),
        branchFilter = selectedBranchFilter
      )
    }

    val totalNominalMatched = matchedTransactions.sumOf { it.price }

    MckaysBottomSheetDialog(
      title = "Export Laporan Transaksi (CSV)",
      onDismiss = { showExportCsvModal = false }
    ) {
      // Period Toggle (Harian / Bulanan / Semua)
      Text(
        text = "Pilih Periode Laporan:",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        CsvExporter.PeriodType.values().forEach { period ->
          val isSelected = selectedPeriod == period
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) RoyalBluePrimary else MaterialTheme.colorScheme.surfaceVariant)
              .clickable { selectedPeriod = period }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = period.label,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Input for Date / Month based on Period selection
      when (selectedPeriod) {
        CsvExporter.PeriodType.DAILY -> {
          Text("Tanggal Penjualan (yyyy-MM-dd):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = inputDate,
            onValueChange = { inputDate = it },
            placeholder = { Text("Contoh: 2026-11-08") },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("export_csv_date_input")
          )
        }
        CsvExporter.PeriodType.MONTHLY -> {
          Text("Bulan Penjualan (yyyy-MM):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = inputMonth,
            onValueChange = { inputMonth = it },
            placeholder = { Text("Contoh: 2026-11") },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("export_csv_month_input")
          )
        }
        CsvExporter.PeriodType.ALL -> {
          Text(
            text = "Semua riwayat transaksi dari seluruh tanggal akan diekspor.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Branch Filter (Staff is locked to current branch; Admin can choose Semua Cabang or specific branch)
      if (currentUser?.isAdmin == true) {
        Text("Filter Cabang:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        var branchExpanded by remember { mutableStateOf(false) }
        val branchOptions = listOf("Semua Cabang") + branches

        ExposedDropdownMenuBox(
          expanded = branchExpanded,
          onExpandedChange = { branchExpanded = !branchExpanded }
        ) {
          OutlinedTextField(
            value = selectedBranchFilter,
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
            branchOptions.forEach { b ->
              DropdownMenuItem(
                text = { Text(b) },
                onClick = {
                  selectedBranchFilter = b
                  branchExpanded = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
      }

      // Summary Card
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = RoyalBlueSurface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Data Terfilter:",
              fontSize = 11.sp,
              color = RoyalBluePrimary,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "${matchedTransactions.size} Transaksi",
              fontSize = 14.sp,
              fontWeight = FontWeight.Black,
              color = RoyalBluePrimary
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "Total Omset:",
              fontSize = 11.sp,
              color = RoyalBluePrimary,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = viewModel.formatCurrency(totalNominalMatched),
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              color = GreenSuccess
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Preview Toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Pratinjau Struktur CSV:",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(
          onClick = { showPreview = !showPreview },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(if (showPreview) "Sembunyikan" else "Buka Pratinjau", fontSize = 11.sp)
        }
      }

      if (showPreview) {
        Spacer(modifier = Modifier.height(8.dp))
        val sampleCsv = remember(matchedTransactions) {
          CsvExporter.generateCsvString(matchedTransactions, selectedPeriod.label)
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Format CSV (RFC 4180)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              IconButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  clipboard.setPrimaryClip(ClipData.newPlainText("CSV Report", sampleCsv))
                  viewModel.showToast("Isi CSV disalin ke papan klip!")
                },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
            ) {
              Text(
                text = sampleCsv.lines().take(8).joinToString("\n") + if (sampleCsv.lines().size > 8) "\n..." else "",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                lineHeight = 14.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Export Button
      Button(
        onClick = {
          if (matchedTransactions.isEmpty()) {
            viewModel.showToast("Tidak ada transaksi pada periode yang dipilih!")
            return@Button
          }

          val filename = when (selectedPeriod) {
            CsvExporter.PeriodType.DAILY -> "mckays_penjualan_harian_${inputDate.trim().ifBlank { todayDate }}.csv"
            CsvExporter.PeriodType.MONTHLY -> "mckays_penjualan_bulanan_${inputMonth.trim().ifBlank { currentMonth }}.csv"
            CsvExporter.PeriodType.ALL -> "mckays_penjualan_semua.csv"
          }

          val result = CsvExporter.exportAndShareCsv(
            context = context,
            transactions = matchedTransactions,
            filename = filename,
            periodLabel = "${selectedPeriod.label} (${if (selectedPeriod == CsvExporter.PeriodType.DAILY) inputDate else if (selectedPeriod == CsvExporter.PeriodType.MONTHLY) inputMonth else "Semua"})"
          )

          if (result.isSuccess) {
            viewModel.showToast("File CSV berhasil dibuat dan siap dibagikan!")
            showExportCsvModal = false
          } else {
            viewModel.showToast("Gagal membuat CSV: ${result.exceptionOrNull()?.message}")
          }
        },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("confirm_export_csv_btn")
      ) {
        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Bagikan / Unduh File CSV (${matchedTransactions.size} Transaksi)",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

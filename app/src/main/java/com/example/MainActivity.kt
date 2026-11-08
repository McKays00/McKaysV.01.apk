package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.NavTab
import com.example.ui.components.MckaysToast
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MemberScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.McKaysTheme
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.RoyalBlueSurface
import com.example.viewmodel.MckaysViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: MckaysViewModel = viewModel()
      val isDarkMode by viewModel.isDarkMode.collectAsState()

      McKaysTheme(darkTheme = isDarkMode) {
        MainApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun MainApp(viewModel: MckaysViewModel) {
  val currentUser by viewModel.currentUser.collectAsState()
  val activeTab by viewModel.activeTab.collectAsState()
  val toastMessage by viewModel.toastMessage.collectAsState()
  val avatarUri by viewModel.avatarUri.collectAsState()
  val avatarZoom by viewModel.avatarZoom.collectAsState()
  val context = LocalContext.current

  // Handle system back button: if not on Home, return to Home
  BackHandler(enabled = currentUser != null && activeTab != NavTab.HOME) {
    viewModel.setTab(NavTab.HOME)
  }

  Box(modifier = Modifier.fillMaxSize()) {
    if (currentUser == null) {
      LoginScreen(viewModel = viewModel)
    } else {
      Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
          // Modern McKays Header
          Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
              .fillMaxWidth()
              .windowInsetsPadding(WindowInsets.statusBars)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "McKays",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Black,
                  color = MaterialTheme.colorScheme.onSurface,
                  letterSpacing = (-0.5).sp
                )
                Text(
                  text = if (currentUser?.isAdmin == true) "Akses: Admin Pusat" else "Cabang: ${currentUser?.branchName}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = RoyalBluePrimary
                )
              }

              // Top right user avatar
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .border(2.dp, RoyalBlueSurface, CircleShape)
                  .background(RoyalBlueSurface)
                  .clickable { viewModel.setTab(NavTab.PROFILE) }
                  .testTag("top_bar_avatar"),
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
                    tint = RoyalBluePrimary,
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
            }
          }
        },
        bottomBar = {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .shadow(16.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface
          ) {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              tonalElevation = 0.dp,
              modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
              val items = listOf(
                Triple(NavTab.HOME, Icons.Default.Home, "Home"),
                Triple(NavTab.CATALOG, Icons.Default.Inventory2, "Katalog"),
                Triple(NavTab.HISTORY, Icons.Default.Description, "Riwayat"),
                Triple(NavTab.MEMBERS, Icons.Default.People, "Member"),
                Triple(NavTab.PROFILE, Icons.Default.Person, "Akun")
              )

              items.forEach { (tab, icon, label) ->
                val selected = activeTab == tab
                NavigationBarItem(
                  selected = selected,
                  onClick = { viewModel.setTab(tab) },
                  icon = {
                    Icon(
                      imageVector = icon,
                      contentDescription = label,
                      modifier = Modifier.size(22.dp)
                    )
                  },
                  label = {
                    Text(
                      text = label,
                      fontSize = 10.sp,
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                  },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RoyalBluePrimary,
                    selectedTextColor = RoyalBluePrimary,
                    indicatorColor = RoyalBlueSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                )
              }
            }
          }
        }
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)
        ) {
          when (activeTab) {
            NavTab.HOME -> HomeScreen(viewModel = viewModel)
            NavTab.CATALOG -> CatalogScreen(viewModel = viewModel)
            NavTab.HISTORY -> HistoryScreen(viewModel = viewModel)
            NavTab.MEMBERS -> MemberScreen(viewModel = viewModel)
            NavTab.PROFILE -> ProfileScreen(viewModel = viewModel)
          }
        }
      }
    }

    // Global Toast Banner
    MckaysToast(
      message = toastMessage,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 84.dp)
    )
  }
}

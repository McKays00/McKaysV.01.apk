package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.RoyalBlueSecondary
import com.example.ui.theme.RoyalBlueSurface
import com.example.viewmodel.MckaysViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
  viewModel: MckaysViewModel,
  modifier: Modifier = Modifier
) {
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var showId by remember { mutableStateOf(false) }
  var showPassword by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Forgot password states
  var forgotMode by remember { mutableStateOf(false) }
  var otpStep by remember { mutableStateOf(false) }
  var otp by remember { mutableStateOf("") }
  var newPassword by remember { mutableStateOf("") }

  val coroutineScope = rememberCoroutineScope()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(16.dp, shape = RoundedCornerShape(32.dp)),
      shape = RoundedCornerShape(32.dp),
      color = MaterialTheme.colorScheme.surface
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(28.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // App Logo Icon
        Box(
          modifier = Modifier
            .size(88.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(RoyalBlueSurface),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(RoyalBluePrimary),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "M",
              color = Color.White,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 26.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "McKays",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Black,
            fontSize = 24.sp
          ),
          color = MaterialTheme.colorScheme.onSurface
        )

        Text(
          text = "Sistem Manajemen & Inventaris",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!forgotMode) {
          // Standard Login Form
          OutlinedTextField(
            value = username,
            onValueChange = {
              username = it
              errorMessage = null
            },
            placeholder = { Text("ID / Username") },
            leadingIcon = {
              Icon(Icons.Default.Person, contentDescription = "ID", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
              IconButton(onClick = { showId = !showId }) {
                Icon(
                  imageVector = if (showId) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = "Toggle ID visibility",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            },
            visualTransformation = if (showId) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedBorderColor = MaterialTheme.colorScheme.outline,
              focusedBorderColor = RoyalBluePrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_username_input")
          )

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = password,
            onValueChange = {
              password = it
              errorMessage = null
            },
            placeholder = { Text("Password") },
            leadingIcon = {
              Icon(Icons.Default.Lock, contentDescription = "Password", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
              IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                  imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = "Toggle password visibility",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
              onDone = {
                if (!isLoading && username.isNotBlank() && password.isNotBlank()) {
                  isLoading = true
                  coroutineScope.launch {
                    delay(600)
                    val success = viewModel.login(username.trim(), password)
                    if (!success) {
                      errorMessage = "ID atau Password salah!"
                    }
                    isLoading = false
                  }
                }
              }
            ),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedBorderColor = MaterialTheme.colorScheme.outline,
              focusedBorderColor = RoyalBluePrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_password_input")
          )

          AnimatedVisibility(visible = errorMessage != null) {
            Text(
              text = errorMessage ?: "",
              color = MaterialTheme.colorScheme.error,
              fontSize = 12.sp,
              modifier = Modifier.padding(top = 8.dp)
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = {
              if (username.isBlank() || password.isBlank()) {
                errorMessage = "Silakan masukkan ID dan Password!"
                return@Button
              }
              isLoading = true
              coroutineScope.launch {
                delay(600)
                val success = viewModel.login(username.trim(), password)
                if (!success) {
                  errorMessage = "ID atau Password salah!"
                }
                isLoading = false
              }
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .testTag("login_submit_button")
          ) {
            if (isLoading) {
              CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
            } else {
              Text(text = "LOGIN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          TextButton(
            onClick = {
              forgotMode = true
              otpStep = false
              errorMessage = null
            },
            modifier = Modifier.testTag("forgot_password_button")
          ) {
            Text(
              text = "Lupa sandi admin pusat?",
              fontSize = 12.sp,
              color = RoyalBlueSecondary,
              fontWeight = FontWeight.SemiBold
            )
          }


        } else {
          // Forgot Password Form
          Text(
            text = "Reset Sandi Admin Pusat",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(16.dp))

          if (!otpStep) {
            Text(
              text = "Klik tombol di bawah untuk mengirim kode OTP simulasi ke email admin.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = {
                viewModel.showToast("Kode OTP simulasi telah dikirim: 1234")
                otpStep = true
              },
              shape = RoundedCornerShape(18.dp),
              colors = ButtonDefaults.buttonColors(containerColor = RoyalBlueSurface, contentColor = RoyalBluePrimary),
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("send_otp_button")
            ) {
              Text("Kirim OTP ke Email", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
          } else {
            OutlinedTextField(
              value = otp,
              onValueChange = { otp = it },
              placeholder = { Text("4 digit OTP (Cth: 1234)") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              shape = RoundedCornerShape(18.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("otp_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
              value = newPassword,
              onValueChange = { newPassword = it },
              placeholder = { Text("Sandi Admin Baru") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
              shape = RoundedCornerShape(18.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("new_password_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = {
                if (otp != "1234") {
                  viewModel.showToast("Kode OTP salah! (Gunakan 1234)")
                  return@Button
                }
                if (newPassword.isBlank()) {
                  viewModel.showToast("Sandi baru wajib diisi!")
                  return@Button
                }
                viewModel.resetAdminPassword(otp, newPassword)
                forgotMode = false
                otpStep = false
                otp = ""
                newPassword = ""
              },
              shape = RoundedCornerShape(18.dp),
              colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_new_password_button")
            ) {
              Text("Simpan Sandi Baru", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          TextButton(
            onClick = {
              forgotMode = false
              otpStep = false
            }
          ) {
            Text("Batal & Kembali ke Login", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}

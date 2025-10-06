package com.pri2025.pri2025study

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pri2025.pri2025study.ui.theme.Pri2025StudyTheme

// ---- Activity (required by the manifest) ----
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pri2025StudyTheme {
                var loggedIn by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { CenterAlignedTopAppBar(title = { Text("StudyTime") }) }
                ) { innerPadding ->
                    if (!loggedIn) {
                        Greeting(
                            name = "Android",
                            onSignedIn = { loggedIn = true },   // ← flip to Dashboard
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        DashboardScreen()
                    }
                }
            }
        }
    }
}

// ---- Your login/sign-in UI (kept as Greeting) ----
@Composable
fun Greeting(
    name: String,
    onSignedIn: () -> Unit = {},                // ← added callback with default
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    fun submit() {
        showError = email.isBlank() || password.isBlank()
        if (!showError) {
            onSignedIn()                         // ← call to move to Dashboard
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; if (showError) showError = false },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; if (showError) showError = false },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (showError) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Enter email and password",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { submit() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) { Text("Sign in") }

        TextButton(
            onClick = { /* TODO: navigate to Sign Up later */ },
            modifier = Modifier.align(Alignment.End)
        ) { Text("Create account") }
    }
}

// ---- Dashboard screen (weekly minutes + deadlines) ----
@Composable
fun DashboardScreen() {
    data class Deadline(val id: Long, val title: String, val dueText: String)

    val deadlines = remember { mutableStateListOf<Deadline>() }
    var showDeadlineDialog by remember { mutableStateOf(false) }

    var weeklyMinutes by remember { mutableStateOf(0) }
    var addMinutesText by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("This Week", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Total studied: ${weeklyMinutes} minutes")

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = addMinutesText,
                onValueChange = { addMinutesText = it.filter { c -> c.isDigit() } },
                label = { Text("Add minutes") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                val m = addMinutesText.toIntOrNull() ?: 0
                if (m > 0) { weeklyMinutes += m; addMinutesText = "" }
            }) { Text("Add") }
        }

        Spacer(Modifier.height(12.dp))
        Button(onClick = { weeklyMinutes += 30 }) { Text("+30 min quick add") }

        Spacer(Modifier.height(24.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        Text("Upcoming Deadlines", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Button(onClick = { showDeadlineDialog = true }) { Text("Add deadline") }
        Spacer(Modifier.height(12.dp))

        if (deadlines.isEmpty()) {
            Text("No deadlines yet.")
        } else {
            deadlines.forEach { d ->
                ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(d.title, style = MaterialTheme.typography.titleSmall)
                        Text("Due: ${d.dueText}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    if (showDeadlineDialog) {
        var title by remember { mutableStateOf("") }
        var due by remember { mutableStateOf("YYYY-MM-DD") }

        AlertDialog(
            onDismissRequest = { showDeadlineDialog = false },
            title = { Text("Add deadline") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                    OutlinedTextField(value = due, onValueChange = { due = it }, label = { Text("Due date (text)") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val safeTitle = if (title.isBlank()) "Untitled" else title
                    val safeDue = if (due.isBlank()) "TBD" else due
                    deadlines += Deadline(System.currentTimeMillis(), safeTitle, safeDue)
                    showDeadlineDialog = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showDeadlineDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun PomodoroTimer(
    studyMinutes: Int = 30,
    breakMinutes: Int = 10
) {
    // phase & timer state
    var isRunning by remember { mutableStateOf(false) }
    var isStudyPhase by remember { mutableStateOf(true) }  // true = Study, false = Break
    val studySeconds = studyMinutes * 60
    val breakSeconds = breakMinutes * 60
    var remaining by remember { mutableStateOf(studySeconds) }

    // tick every second while running
    LaunchedEffect(isRunning, isStudyPhase, remaining) {
        if (isRunning && remaining > 0) {
            kotlinx.coroutines.delay(1000)
            remaining -= 1
        } else if (isRunning && remaining == 0) {
            // phase finished → auto-switch
            isStudyPhase = !isStudyPhase
            remaining = if (isStudyPhase) studySeconds else breakSeconds
        }
    }

    val total = if (isStudyPhase) studySeconds else breakSeconds
    val progress = (total - remaining).toFloat() / total.toFloat()

    // UI
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (isStudyPhase) "Study (30 min)" else "Break (10 min)",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))

        // Time as MM:SS
        val mm = remaining / 60
        val ss = remaining % 60
        Text(String.format("%02d:%02d", mm, ss), style = MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { isRunning = true },
                modifier = Modifier.weight(1f)
            ) { Text("Start") }

            OutlinedButton(
                onClick = { isRunning = false },
                modifier = Modifier.weight(1f)
            ) { Text("Pause") }

            OutlinedButton(
                onClick = {
                    isRunning = false
                    isStudyPhase = true
                    remaining = studySeconds
                },
                modifier = Modifier.weight(1f)
            ) { Text("Reset") }

            OutlinedButton(
                onClick = {
                    // jump to next phase
                    isStudyPhase = !isStudyPhase
                    remaining = if (isStudyPhase) studySeconds else breakSeconds
                },
                modifier = Modifier.weight(1f)
            ) { Text("Skip") }
        }
    }
}



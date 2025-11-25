package com.pri2025.pri2025study

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pri2025.pri2025study.database.Deadline
import com.pri2025.pri2025study.database.StudyDatabase
import com.pri2025.pri2025study.database.StudySession
import com.pri2025.pri2025study.ui.theme.Pri2025StudyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import android.app.DatePickerDialog
import androidx.compose.foundation.clickable




//  MAIN ACTIVITY
class MainActivity : ComponentActivity() {

    private val db by lazy { StudyDatabase.getInstance(applicationContext) }

    private val studyViewModel: StudyViewModel by viewModels {
        StudyViewModelFactory(db)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 🎵 Start background music
        MusicPlayer.start(this)

        setContent {
            Pri2025StudyTheme {
                AppNavigation(studyViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicPlayer.stop() // stop music when app closes
    }
}


//  NAVIGATION + DATABASE SETUP
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(studyViewModel: StudyViewModel) {
    val navController = rememberNavController()
    var loggedIn by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = remember { StudyDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    // Weekly study minutes (Mon..Sun)
    val weekMinutes = remember { mutableStateListOf(0, 0, 0, 0, 0, 0, 0) }
    val deadlines = remember { mutableStateListOf<Deadline>() }

    // Load from DB once when app starts
    LaunchedEffect(Unit) {
        // load deadlines
        val storedDeadlines = db.deadlineDao().getAll()
        deadlines.clear()
        deadlines.addAll(storedDeadlines)

        // load weekly totals from StudySession table
        val totals = db.studySessionDao().getTotalsByDay()
        // reset to 0
        for (i in 0..6) weekMinutes[i] = 0
        // fill from DB
        totals.forEach { total ->
            if (total.dayIndex in 0..6) {
                weekMinutes[total.dayIndex] = total.totalMinutes
            }
        }
    }

    Scaffold(
        topBar = {
            if (loggedIn) {
                CenterAlignedTopAppBar(
                    title = { Text("StudyTime 🧠") },
                    actions = {
                        TextButton(onClick = { navController.navigate("dashboard") }) { Text("Dashboard") }
                        TextButton(onClick = { navController.navigate("weekly") }) { Text("Weekly") }
                        TextButton(onClick = { navController.navigate("addDeadline") }) { Text("Deadlines") }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (loggedIn) "dashboard" else "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                Greeting(
                    onSignedIn = {
                        loggedIn = true
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            composable("dashboard") {
                DashboardScreen(
                    totalWeeklyMinutes = weekMinutes.sum(),
                    onAddMinutes = { m ->
                        // update current day in UI
                        val i = currentDayIndexMon0()
                        weekMinutes[i] = weekMinutes[i] + m

                        // also save to DB
                        scope.launch {
                            db.studySessionDao().insert(
                                StudySession(
                                    dayIndex = i,
                                    minutes = m
                                )
                            )
                        }
                    },
                    onAddDeadlineClick = { navController.navigate("addDeadline") },
                    studyViewModel = studyViewModel
                )
            }
            composable("weekly") {
                WeeklyReportScreen(weekMinutes = weekMinutes)
            }
            composable("addDeadline") {
                AddDeadlineScreen(
                    deadlines = deadlines,
                    onAddDeadline = { title, due ->
                        val safeTitle = if (title.isBlank()) "Untitled" else title
                        val safeDue = if (due.isBlank()) "TBD" else due
                        scope.launch {
                            val entity = Deadline(title = safeTitle, dueText = safeDue)
                            val id = db.deadlineDao().insert(entity)
                            deadlines.add(0, entity.copy(id = id))
                        }
                    }
                )
            }
        }
    }
}

// helper to detect current route (still here if you want to use it later)
@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

// Map Calendar day to Mon=0..Sun=6
fun currentDayIndexMon0(): Int {
    val c = Calendar.getInstance()
    return when (c.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        else -> 6 // Sunday
    }
}

// LOGIN SCREEN – more playful + app "logo"
@Composable
fun Greeting(onSignedIn: () -> Unit = {}) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    fun submit() {
        showError = email.isBlank() || password.isBlank()
        if (!showError) onSignedIn()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🧠 StudyTime", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Help you to stay focused and motivated ✨",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; if (showError) showError = false },
                label = { Text("Uni email") },
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
            ) { Text("Sign in 🚀") }

            Spacer(Modifier.height(12.dp))
            Text(
                "Tip: Use your uni email so future you remembers 😄",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

//  DASHBOARD SCREEN – slightly more playful
@Composable
fun DashboardScreen(
    totalWeeklyMinutes: Int,
    onAddMinutes: (Int) -> Unit,
    onAddDeadlineClick: () -> Unit,
    studyViewModel: StudyViewModel
) {
    val context = LocalContext.current
    var addMinutesText by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("This Week 📚", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Total studied: ${totalWeeklyMinutes} minutes", style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(16.dp))
        Text("Focus timer 🎯", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        PomodoroTimer(studyMinutes = 30, breakMinutes = 10)

        // 🔊 MUSIC CONTROLS
        Spacer(Modifier.height(16.dp))
        Text("Study music 🎵", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { MusicPlayer.start(context) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Play music")
            }
            OutlinedButton(
                onClick = { MusicPlayer.pause() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Pause ⏸️")
            }
        }
        Spacer(Modifier.height(24.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        Text("Quick Add Minutes ⏱️", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = addMinutesText,
                onValueChange = { addMinutesText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Add minutes") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                val m = addMinutesText.toIntOrNull() ?: 0
                if (m > 0) {
                    onAddMinutes(m)
                    addMinutesText = ""
                }
            }) { Text("Add") }
        }

        Spacer(Modifier.height(12.dp))
        Button(onClick = { onAddMinutes(30) }) { Text("+30 min quick add 💪") }

        Spacer(Modifier.height(24.dp))
        Divider()
        Spacer(Modifier.height(12.dp))
        Text("Upcoming Deadlines 📅", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onAddDeadlineClick() }) { Text("Add or View Deadlines") }
    }
}


// weekly report screen
@Composable
fun WeeklyReportScreen(weekMinutes: List<Int>) {
    val labels = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
    val total = weekMinutes.sum()
    val maxDay = (weekMinutes.maxOrNull() ?: 0).coerceAtLeast(1)

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Weekly Study Summary 📊", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Total: ${total} minutes", style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        for (i in 0..6) {
            Text("${labels[i]} — ${weekMinutes[i]} min", style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(
                progress = (weekMinutes[i].toFloat() / maxDay.toFloat()).coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(12.dp))
        val avg = (total / 7.0).toInt()
        Text("Daily average: ${avg} min/day", style = MaterialTheme.typography.bodyMedium)
    }
}

// DEADLINE SCREEN – powered by DB via callback
@Composable
fun AddDeadlineScreen(
    deadlines: List<Deadline>,
    onAddDeadline: (String, String) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("All Deadlines 📅", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        Button(onClick = { showAddDialog = true }) { Text("Add New Deadline") }

        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        if (deadlines.isEmpty()) {
            Text("No deadlines yet. Add your first one to stay ahead 😌")
        } else {
            deadlines.forEach { d ->
                ElevatedCard(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(d.title, style = MaterialTheme.typography.titleSmall)
                        Text("Due: ${d.dueText}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var due by remember { mutableStateOf("") }

        // helper to open the calendar
        fun openDatePicker() {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                context,
                { _, y, m, d ->
                    // format as YYYY-MM-DD
                    due = String.format("%04d-%02d-%02d", y, m + 1, d)
                },
                year,
                month,
                day
            ).show()
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Deadline") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = due,
                        onValueChange = { },
                        label = { Text("Due date") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("No date selected") }
                    )

                    // SIMPLE BUTTON TO OPEN CALENDAR
                    Button(
                        onClick = { openDatePicker() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pick date 📅")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val safeTitle = if (title.isBlank()) "Untitled" else title
                    val safeDue = if (due.isBlank()) "TBD" else due
                    onAddDeadline(safeTitle, safeDue)
                    showAddDialog = false
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}



//  POMODORO TIMER – same logic, slightly more fun labels
@Composable
fun PomodoroTimer(
    studyMinutes: Int = 30,
    breakMinutes: Int = 10
) {
    var isRunning by remember { mutableStateOf(false) }
    var isStudyPhase by remember { mutableStateOf(true) }
    val studySeconds = studyMinutes * 60
    val breakSeconds = breakMinutes * 60
    var remaining by remember { mutableStateOf(studySeconds) }

    LaunchedEffect(isRunning, isStudyPhase, remaining) {
        if (isRunning && remaining > 0) {
            delay(1000)
            remaining -= 1
        } else if (isRunning && remaining == 0) {
            isStudyPhase = !isStudyPhase
            remaining = if (isStudyPhase) studySeconds else breakSeconds
        }
    }

    val total = if (isStudyPhase) studySeconds else breakSeconds
    val progress = (total - remaining).toFloat() / total.toFloat()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (isStudyPhase) "Focus time (30 min) 🔥" else "Break time (10 min) 🧃",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
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
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { isRunning = true }, modifier = Modifier.weight(1f)) { Text("Start") }
            OutlinedButton(onClick = { isRunning = false }, modifier = Modifier.weight(1f)) { Text("Pause") }
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
                    isStudyPhase = !isStudyPhase
                    remaining = if (isStudyPhase) studySeconds else breakMinutes * 60
                },
                modifier = Modifier.weight(1f)
            ) { Text("Skip") }
        }
    }
}

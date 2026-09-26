package com.custodian.android

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private data class Story(val title: String, val category: String, val summary: String, val body: String)

private fun decodeStory(value: String): Story? {
    return try {
        val json = org.json.JSONObject(value)
        Story(
            json.getString("title"),
            json.getString("category"),
            json.getString("summary"),
            json.getString("body")
        )
    } catch (_: Exception) {
        null
    }
}

private val stories = listOf(
    Story("The Village Elder", "Culture", "A community story about wisdom, responsibility, and protecting tradition.", "At the heart of the village stood an elder who believed that knowledge was strongest when it was shared. Families gathered to listen, ask questions, and remember the lessons of those who came before them."),
    Story("The Lost Path", "Community", "A family discovers why listening to elders can prevent a difficult journey.", "A young family set out on an unfamiliar path and soon realized that the road was difficult. An older relative remembered a safer route and guided them back. The experience taught the family the value of patience, experience, and communication."),
    Story("Our Heritage", "Tradition", "A simple introduction to preserving language, customs, and family history.", "Young people began recording the words, songs, recipes, and memories shared by their grandparents. They discovered that heritage can be carried forward through everyday conversations and respectful storytelling.")
)


private const val CHECK_IN_REMINDER_REQUEST = 4101

private fun setCheckInReminder(context: Context, enabled: Boolean) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, CheckInReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        CHECK_IN_REMINDER_REQUEST,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (!enabled) {
        alarmManager.cancel(pendingIntent)
        return
    }

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 20)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
    }

    alarmManager.setInexactRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CustodianApp() }
    }
}

@Composable
private fun CustodianApp() {
    var screen by remember { mutableStateOf("home") }
    var selectedStory by remember { mutableStateOf(stories.first()) }
    val context = LocalContext.current
    val repository = remember(context) { SafetyRepository(context) }
    var profileState by remember { mutableStateOf(repository.load()) }

    MaterialTheme {
        Scaffold(
            bottomBar = {
                if (screen != "storyDetail") {
                    NavigationBar {
                        NavigationBarItem(
                            selected = screen == "home",
                            onClick = { screen = "home" },
                            icon = { Text("⌂") },
                            label = { Text("Home") }
                        )
                        NavigationBarItem(
                            selected = screen == "stories",
                            onClick = { screen = "stories" },
                            icon = { Text("📖") },
                            label = { Text("Stories") }
                        )
                        NavigationBarItem(
                            selected = screen == "safety",
                            onClick = { screen = "safety" },
                            icon = { Text("✓") },
                            label = { Text("Safety") }
                        )
                        NavigationBarItem(
                            selected = screen == "profile",
                            onClick = { screen = "profile" },
                            icon = { Text("●") },
                            label = { Text("Profile") }
                        )
                    }
                }
            }
        ) { padding ->
            Surface(Modifier.fillMaxSize().padding(padding)) {
                when (screen) {
                    "stories" -> StoriesScreen({ screen = "home" }) { story -> selectedStory = story; screen = "storyDetail" }
                    "storyDetail" -> StoryDetailScreen(selectedStory) { screen = "stories" }
                    "safety" -> SafetyScreen { screen = "home" }
                    "profile" -> ProfileScreen(profileState, { name, about ->
                        profileState = profileState.copy(profileName = name, profileAbout = about)
                        repository.save(profileState)
                    }) { screen = "home" }
                    else -> HomeScreen({ screen = "stories" }, { screen = "safety" })
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(onStories: () -> Unit, onSafety: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("CUSTODIAN", style = MaterialTheme.typography.headlineLarge)
        Text("Our stories. Our wisdom. Our future.")
        Spacer(Modifier.height(20.dp))

        Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Welcome home", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text("Your place for community stories, cultural knowledge, and family safety.")
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Community Stories", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(4.dp))
                        Text("Read stories and preserve knowledge from one generation to the next.")
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = onStories) { Text("Explore stories") }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Family Safety", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(4.dp))
                        Text("Check in, manage your trusted contact, and complete your safety checklist.")
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(onClick = onSafety) { Text("Open safety center") }
                    }
                }
            }
            item {
                Text(
                    "Privacy first • Your safety information stays on this device.",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun StoriesScreen(onBack: () -> Unit, onStory: (Story) -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { SafetyRepository(context) }
    var userStories by remember { mutableStateOf(repository.load().userStories.mapNotNull(::decodeStory)) }
    val allStories = stories + userStories
    val categories = listOf("All") + allStories.map { it.category }.distinct()
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }
    var savedOnly by remember { mutableStateOf(false) }
    val savedTitles = remember { repository.load().savedStories }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Community") }
    var summary by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    val filteredStories = allStories.filter { story ->
        val matchesCategory = selectedCategory == "All" || story.category == selectedCategory
        val query = searchQuery.trim()
        val matchesSearch = query.isBlank() || listOf(story.title, story.category, story.summary, story.body)
            .any { it.contains(query, ignoreCase = true) }
        val matchesSaved = !savedOnly || story.title in savedTitles
        matchesCategory && matchesSearch && matchesSaved
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Community Stories", style = MaterialTheme.typography.headlineMedium)
        Text("Explore and preserve community knowledge.")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showCreate = true }) { Text("Create a story") }
            OutlinedButton(onClick = { savedOnly = !savedOnly }) {
                Text(if (savedOnly) "All stories" else "Saved stories")
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search stories") },
            placeholder = { Text("Title, category, or keyword") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { current ->
                if (current == selectedCategory) Button(onClick = { selectedCategory = current }) { Text(current) }
                else OutlinedButton(onClick = { selectedCategory = current }) { Text(current) }
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredStories) { story ->
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(story.title, style = MaterialTheme.typography.titleLarge)
                        Text(story.category, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(story.summary)
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(onClick = { onStory(story) }) { Text("Read story") }
                    }
                }
            }
            item { Button(onClick = onBack) { Text("Back") } }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Create Community Story") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true)
                    OutlinedTextField(category, { category = it }, label = { Text("Category") }, singleLine = true)
                    OutlinedTextField(summary, { summary = it }, label = { Text("Short summary") })
                    OutlinedTextField(body, { body = it }, label = { Text("Story") }, minLines = 4)
                }
            },
            confirmButton = {
                Button(
                    enabled = title.isNotBlank() && category.isNotBlank() && body.isNotBlank(),
                    onClick = {
                        val story = Story(title.trim(), category.trim(), summary.trim(), body.trim())
                        val json = org.json.JSONObject().apply {
                            put("title", story.title)
                            put("category", story.category)
                            put("summary", story.summary)
                            put("body", story.body)
                        }.toString()
                        val state = repository.load()
                        repository.save(state.copy(userStories = state.userStories + json))
                        userStories = userStories + story
                        title = ""
                        category = "Community"
                        summary = ""
                        body = ""
                        showCreate = false
                    }
                ) { Text("Publish") }
            },
            dismissButton = { OutlinedButton(onClick = { showCreate = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ProfileScreen(state: SafetyState, onSave: (String, String) -> Unit, onBack: () -> Unit) {
    var name by remember(state.profileName) { mutableStateOf(state.profileName) }
    var about by remember(state.profileAbout) { mutableStateOf(state.profileAbout) }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("My Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text(if (name.isBlank()) "Welcome to Custodian" else name, style = MaterialTheme.typography.titleLarge)
                Text(if (about.isBlank()) "Tell the community a little about yourself." else about)
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Your name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(about, { about = it }, label = { Text("About you") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onSave(name.trim(), about.trim()) }, modifier = Modifier.fillMaxWidth()) { Text("Save profile") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}

@Composable
private fun StoryDetailScreen(story: Story, onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { SafetyRepository(context) }
    var saved by remember(story.title) { mutableStateOf(story.title in repository.load().savedStories) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(story.title, style = MaterialTheme.typography.headlineMedium)
        Text("${story.category} • Custodian Stories")
        Spacer(Modifier.height(20.dp))
        Text(story.summary, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Text(story.body, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val state = repository.load()
                val updated = if (saved) state.savedStories - story.title else state.savedStories + story.title
                repository.save(state.copy(savedStories = updated))
                saved = !saved
            }) {
                Text(if (saved) "Saved ✓" else "Save story")
            }
            OutlinedButton(onClick = {
                val message = story.title + "\n\n" + story.summary + "\n\n" + story.body
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                }, "Share story"))
            }) { Text("Share") }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack) { Text("Back to stories") }
    }
}
@Composable
private fun SafetyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { SafetyRepository(context) }
    var state by remember { mutableStateOf(repository.load()) }
    val checklist = listOf(
        "Keep important family contacts available",
        "Agree on a family meeting point",
        "Keep emergency information accessible",
        "Check that children know who they can ask for help"
    )

    var showContactForm by remember { mutableStateOf(false) }
    var contactName by remember { mutableStateOf(state.emergencyContactName) }
    var contactPhone by remember { mutableStateOf(state.emergencyContactPhone) }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Family Safety", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(6.dp))
        Text("Your safety information is stored locally on this device.")
        Spacer(Modifier.height(12.dp))

        Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Daily Check-In Reminder", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text("Get a daily reminder at 8:00 PM to confirm that you are safe.")
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (state.reminderEnabled) "Reminder is on" else "Reminder is off")
                    Switch(checked = state.reminderEnabled, onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= 33) {
                            (context as? Activity)?.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 4102)
                        }
                        state = state.copy(reminderEnabled = enabled)
                        repository.save(state)
                        setCheckInReminder(context, enabled)
                    })
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Safety Check-In", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text(if (state.lastCheckIn.isBlank()) "Let your trusted people know you are safe." else "Last check-in: ${state.lastCheckIn}")
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        val timestamp = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
                        val history = (listOf(timestamp) + state.checkInHistory).distinct().take(10)
                        state = state.copy(lastCheckIn = timestamp, checkInHistory = history)
                        repository.save(state)
                    }) { Text("I am Safe") }
                    if (state.lastCheckIn.isNotBlank()) {
                        OutlinedButton(onClick = {
                            val message = "Custodian safety check-in: I am safe. Last checked in at ${state.lastCheckIn}."
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, message)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share check-in"))
                        }) { Text("Share") }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (state.checkInHistory.isNotEmpty()) {
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Check-In History", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    state.checkInHistory.take(5).forEach { entry ->
                        Text("• $entry", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Emergency Contact", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                if (state.emergencyContactName.isBlank()) {
                    Text("Add a trusted person you can contact quickly.")
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { showContactForm = true }) { Text("Add contact") }
                } else {
                    Text(state.emergencyContactName, style = MaterialTheme.typography.titleMedium)
                    Text(state.emergencyContactPhone)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            val phone = Uri.encode(state.emergencyContactPhone)
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                        }) { Text("Call") }
                        OutlinedButton(onClick = {
                            val phone = Uri.encode(state.emergencyContactPhone)
                            val message = Uri.encode("Custodian emergency alert: I need help. Please contact me as soon as possible.")
                            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
                                putExtra("sms_body", message)
                            })
                        }) { Text("Emergency SMS") }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { showContactForm = true }) { Text("Edit contact") }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (showContactForm) {
            AlertDialog(
                onDismissRequest = { showContactForm = false },
                title = { Text("Emergency Contact") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = contactName, onValueChange = { contactName = it }, label = { Text("Name") }, singleLine = true)
                        OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = { Text("Phone number") }, singleLine = true)
                    }
                },
                confirmButton = {
                    Button(enabled = contactName.isNotBlank() && contactPhone.isNotBlank(), onClick = {
                        state = state.copy(emergencyContactName = contactName.trim(), emergencyContactPhone = contactPhone.trim())
                        repository.save(state)
                        showContactForm = false
                    }) { Text("Save") }
                },
                dismissButton = { OutlinedButton(onClick = { showContactForm = false }) { Text("Cancel") } }
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(checklist.indices.toList()) { index ->
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(checklist[index], Modifier.weight(1f).padding(end = 8.dp))
                        Checkbox(
                            checked = index in state.completedSteps,
                            onCheckedChange = { checked ->
                                val completed = if (checked) {
                                    state.completedSteps + index
                                } else {
                                    state.completedSteps - index
                                }
                                state = state.copy(completedSteps = completed)
                                repository.save(state)
                            }
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "${state.completedSteps.size} of ${checklist.size} safety steps completed",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Back") }
            }
        }
    }
}

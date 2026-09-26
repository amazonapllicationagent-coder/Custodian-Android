package com.custodian.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import java.text.SimpleDateFormat
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

private data class Story(val title: String, val category: String, val summary: String)

private val stories = listOf(
    Story("The Village Elder", "Culture", "A community story about wisdom, responsibility, and protecting tradition."),
    Story("The Lost Path", "Community", "A family discovers why listening to elders can prevent a difficult journey."),
    Story("Our Heritage", "Tradition", "A simple introduction to preserving language, customs, and family history.")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CustodianApp() }
    }
}

@Composable
private fun CustodianApp() {
    var screen by remember { mutableStateOf("home") }

    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            when (screen) {
                "stories" -> StoriesScreen({ screen = "home" }) { screen = "storyDetail" }
                "storyDetail" -> StoryDetailScreen { screen = "stories" }
                "safety" -> SafetyScreen { screen = "home" }
                else -> HomeScreen({ screen = "stories" }, { screen = "safety" })
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
private fun StoriesScreen(onBack: () -> Unit, onStory: () -> Unit) {
    val storyTitleStyle = MaterialTheme.typography.titleLarge
    val storyCategoryStyle = MaterialTheme.typography.labelMedium

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Community Stories", style = MaterialTheme.typography.headlineMedium)
        Text("Explore stories and cultural knowledge.")
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(stories) { story ->
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(story.title, style = storyTitleStyle)
                        Text(story.category, style = storyCategoryStyle)
                        Spacer(Modifier.height(6.dp))
                        Text(story.summary)
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(onClick = onStory) { Text("Read story") }
                    }
                }
            }
            item { Button(onClick = onBack) { Text("Back") } }
        }
    }
}

@Composable
private fun StoryDetailScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("The Village Elder", style = MaterialTheme.typography.headlineMedium)
        Text("Culture • Community")
        Spacer(Modifier.height(20.dp))
        Text("Every community carries knowledge from one generation to the next. This story space will eventually contain full chapters, audio, images, and creator information.")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack) { Text("Back to stories") }
    }
}

@Composable
private fun SafetyScreen(onBack: () -> Unit) {
    val repository = remember { SafetyRepository(LocalContext.current) }
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
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Family Safety", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(6.dp))
        Text("Your safety information is stored locally on this device.")
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
                        state = state.copy(lastCheckIn = timestamp)
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
                        OutlinedButton(onClick = { showContactForm = true }) { Text("Edit") }
                    }
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

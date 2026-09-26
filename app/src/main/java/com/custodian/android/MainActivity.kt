package com.custodian.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("CUSTODIAN", style = MaterialTheme.typography.headlineLarge)
        Text("Our stories. Our wisdom. Our future.")
        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Welcome", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text("Discover community stories, preserve cultural knowledge, and access family safety tools.")
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onStories) { Text("Stories") }
            OutlinedButton(onClick = onSafety) { Text("Safety") }
        }
    }
}

@Composable
private fun StoriesScreen(onBack: () -> Unit, onStory: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Community Stories", style = MaterialTheme.typography.headlineMedium)
        Text("Explore stories and cultural knowledge.")
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(stories) { story ->
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(story.title, style = MaterialTheme.typography.titleLarge)
                        Text(story.category, style = MaterialTheme.typography.labelMedium)
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

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Family Safety", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(6.dp))
        Text("Your checklist is stored locally on this device.")
        Spacer(Modifier.height(16.dp))

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

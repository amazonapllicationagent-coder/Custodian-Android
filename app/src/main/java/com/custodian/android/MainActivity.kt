package com.custodian.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
                "stories" -> StoriesScreen(
                    onBack = { screen = "home" },
                    onStory = { screen = "storyDetail" }
                )
                "storyDetail" -> StoryDetailScreen(onBack = { screen = "stories" })
                "safety" -> SafetyScreen(onBack = { screen = "home" })
                else -> HomeScreen(
                    onStories = { screen = "stories" },
                    onSafety = { screen = "safety" }
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(onStories: () -> Unit, onSafety: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        Text("CUSTODIAN", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
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
        Spacer(Modifier.height(6.dp))
        Text("Explore stories and cultural knowledge.")
        Spacer(Modifier.height(16.dp))

        stories.forEach { story ->
            Card(
                Modifier.fillMaxWidth().padding(bottom = 12.dp),
                RoundedCornerShape(16.dp)
            ) {
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

        Button(onClick = onBack) { Text("Back") }
    }
}

@Composable
private fun StoryDetailScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("The Village Elder", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Culture • Community")
        Spacer(Modifier.height(20.dp))
        Text(
            "Every community carries knowledge from one generation to the next. " +
                "This story space will eventually contain full chapters, audio, images, " +
                "and creator information."
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack) { Text("Back to stories") }
    }
}

@Composable
private fun SafetyScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Family Safety", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text("Safety features will be added here with privacy, permissions, and user control at the center.")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}

package com.custodian.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
                "stories" -> StoriesScreen(onBack = { screen = "home" })
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
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("CUSTODIAN", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text("Our stories. Our wisdom. Our future.")
        Spacer(Modifier.height(28.dp))

        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Welcome", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text("A trusted place for community stories, family safety, and cultural knowledge.")
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
private fun StoriesScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Community Stories", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text("Stories and cultural knowledge can be organized here as the app grows.")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}

@Composable
private fun SafetyScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Family Safety", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text("Safety tools will be added here, with privacy and user control built in.")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}

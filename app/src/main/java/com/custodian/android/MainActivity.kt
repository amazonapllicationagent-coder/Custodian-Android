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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class RootMember(val name: String, val relationship: String, val community: String)
data class ElderStory(val elder: String, val title: String, val community: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CustodianApp() }
    }

    @Composable
    private fun CustodianApp() {
        var signedIn by remember { mutableStateOf(false) }
        var selected by remember { mutableStateOf(0) }
        val roots = remember { mutableStateListOf<RootMember>() }
        val stories = remember { mutableStateListOf<ElderStory>() }

        MaterialTheme {
            if (!signedIn) {
                WelcomeScreen(
                    onCreateAccount = { signedIn = true }
                )
                return@MaterialTheme
            }

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        listOf("Home", "My Roots", "Elders", "Timeline", "Archive").forEachIndexed { index, label ->
                            NavigationBarItem(
                                selected = selected == index,
                                onClick = { selected = index },
                                icon = { Text(label.take(1)) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
                ) {
                    when (selected) {
                        0 -> Dashboard(onSignOut = { signedIn = false })
                        1 -> FamilyScreen(roots)
                        2 -> EldersScreen(stories)
                        3 -> TimelineScreen()
                        else -> ArchiveScreen()
                    }
                }
            }
        }
    }

    @Composable
    private fun WelcomeScreen(onCreateAccount: () -> Unit, onContinue: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("👑", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(16.dp))
            Text("Welcome to Custodian", style = MaterialTheme.typography.headlineMedium)
            Text("Roots • Memory • Legacy", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Text("Preserve your family history, oral traditions, elders' stories and cultural memory in one place.")
            Spacer(Modifier.height(24.dp))
            Button(onClick = onCreateAccount, modifier = Modifier.fillMaxWidth()) {
                Text("Create account / Sign in")
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text("Open Custodian")
            }
            Spacer(Modifier.height(12.dp))
            Text("Your account is managed securely through Custodian.")
        }
    }

    @Composable
    private fun Dashboard(onSignOut: () -> Unit) {
        Text("Custodian", style = MaterialTheme.typography.headlineMedium)
        Text("Your heritage, kept for the next generation.", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(20.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("My Heritage", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text("Family Tree • Elder Stories • Omenala • Timeline • Archive")
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { selected = 1 }, modifier = Modifier.fillMaxWidth()) {
            Text("Explore my roots")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text("Sign out")
        }
    }

    @Composable
    private fun FamilyScreen(roots: MutableList<RootMember>) {
        Text("My Roots", style = MaterialTheme.typography.headlineSmall)
        Text("Build and preserve your family tree.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        Button(onClick = { roots.add(RootMember("New family member", "Relative", "My community")) }) {
            Text("Add family member")
        }
        Spacer(Modifier.height(12.dp))
        if (roots.isEmpty()) {
            Text("No family members added yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(roots) { member ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text(member.name, style = MaterialTheme.typography.titleMedium)
                            Text(member.relationship + " • " + member.community)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun EldersScreen(stories: MutableList<ElderStory>) {
        Text("Our Elders Speak", style = MaterialTheme.typography.headlineSmall)
        Text("Keep oral history alive.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            stories.add(ElderStory("New elder", "New oral history", "My community"))
        }) { Text("Add elder story") }
        Spacer(Modifier.height(12.dp))
        if (stories.isEmpty()) {
            Text("No oral histories saved yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(stories) { story ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text(story.title, style = MaterialTheme.typography.titleMedium)
                            Text(story.elder + " • " + story.community)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TimelineScreen() {
        Text("Timeline", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("Record important events, migrations, traditions and family milestones.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { }) { Text("Add event") }
    }

    @Composable
    private fun ArchiveScreen() {
        Text("Archive", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("Your preserved heritage records will appear here.")
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { }) { Text("Backup") }
            OutlinedButton(onClick = { }) { Text("Restore") }
        }
    }
}

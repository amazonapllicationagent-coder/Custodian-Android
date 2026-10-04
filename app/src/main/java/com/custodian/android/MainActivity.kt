package com.custodian.android

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Forest = Color(0xFF063B2B)
private val DeepForest = Color(0xFF03261D)
private val Gold = Color(0xFFD5A93A)
private val Sand = Color(0xFFF6F1E7)
private val Ink = Color(0xFF1D2A25)

data class RootMember(val name: String, val relationship: String, val community: String)
data class ElderStory(
    val elder: String,
    val title: String,
    val category: String,
    val community: String,
    val transcript: String,
    val audioPath: String? = null
)
data class TimelineEvent(val title: String, val date: String, val description: String)

class MainActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("custodian", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = DeepForest.toArgb()
        window.navigationBarColor = DeepForest.toArgb()
        setContent { CustodianApp() }
    }

    @Composable
    private fun CustodianApp() {
        var signedIn by remember { mutableStateOf(prefs.getBoolean("signed_in", false)) }
        var userName by remember { mutableStateOf(prefs.getString("user_name", "") ?: "") }
        var community by remember { mutableStateOf(prefs.getString("community", "") ?: "") }
        var selected by remember { mutableStateOf(0) }

        val roots = remember {
            mutableStateListOf<RootMember>().also { it.addAll(loadRoots()) }
        }
        val stories = remember {
            mutableStateListOf<ElderStory>().also { it.addAll(loadStories()) }
        }
        val events = remember {
            mutableStateListOf<TimelineEvent>().also { it.addAll(loadEvents()) }
        }

        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Forest,
                onPrimary = Color.White,
                secondary = Gold,
                background = Sand,
                surface = Color.White,
                onBackground = Ink,
                onSurface = Ink
            )
        ) {
            if (!signedIn) {
                WelcomeScreen(
                    onEnter = { name, village ->
                        userName = name
                        community = village
                        prefs.edit()
                            .putBoolean("signed_in", true)
                            .putString("user_name", name)
                            .putString("community", village)
                            .apply()
                        signedIn = true
                    }
                )
            } else {
                Scaffold(
                    containerColor = Sand,
                    bottomBar = {
                        NavigationBar(containerColor = Color.White) {
                            val labels = listOf("Home", "Roots", "Elders", "Timeline", "Archive")
                            val symbols = listOf("⌂", "⌘", "◉", "◷", "▣")
                            labels.forEachIndexed { index, label ->
                                NavigationBarItem(
                                    selected = selected == index,
                                    onClick = { selected = index },
                                    icon = { Text(symbols[index], fontWeight = FontWeight.Bold) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                ) { padding ->
                    when (selected) {
                        0 -> HomeScreen(
                            name = userName,
                            community = community,
                            rootCount = roots.size,
                            storyCount = stories.size,
                            eventCount = events.size,
                            onRoots = { selected = 1 },
                            onElders = { selected = 2 },
                            onTimeline = { selected = 3 },
                            onProfile = {
                                prefs.edit().putBoolean("signed_in", false).apply()
                                signedIn = false
                            }
                        )
                        1 -> RootsScreen(roots)
                        2 -> EldersScreen(stories)
                        3 -> TimelineScreen(events)
                        else -> ArchiveScreen(roots, stories, events)
                    }
                }
            }
        }
    }

    @Composable
    private fun WelcomeScreen(onEnter: (String, String) -> Unit) {
        var name by remember { mutableStateOf("") }
        var village by remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Sand)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            Image(
                painter = painterResource(R.drawable.custodian_heritage),
                contentDescription = "Custodian heritage emblem",
                modifier = Modifier.size(150.dp).clip(RoundedCornerShape(32.dp))
            )
            Spacer(Modifier.height(16.dp))
            Text("Custodian", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = Forest)
            Text("Know your roots. Preserve your legacy.", style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(Modifier.height(12.dp))
            Text(
                "A home for family history, elder stories, traditions, important moments and cultural memory.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(24.dp))
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text("Enter Custodian", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Your name") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = village,
                        onValueChange = { village = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Community / village") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onEnter(name.trim(), village.trim()) },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continue")
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("Built to grow with your family and community.", color = Forest)
        }
    }

    @Composable
    private fun HomeScreen(
        name: String,
        community: String,
        rootCount: Int,
        storyCount: Int,
        eventCount: Int,
        onRoots: () -> Unit,
        onElders: () -> Unit,
        onTimeline: () -> Unit,
        onProfile: () -> Unit
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Welcome, ${name.ifBlank { "Custodian" }}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            community.ifBlank { "Your heritage space" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Forest
                        )
                    }
                    OutlinedButton(onClick = onProfile, contentPadding = PaddingValues(horizontal = 12.dp)) {
                        Text("Sign out")
                    }
                }
            }
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(225.dp)
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    Image(
                        painter = painterResource(R.drawable.custodian_heritage),
                        contentDescription = "Custodian heritage artwork",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        Modifier.align(Alignment.BottomStart).padding(20.dp)
                    ) {
                        Text("Your roots. Your story. Your legacy.", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Preserve what should not be forgotten.", color = Color.White)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    SummaryCard("Roots", rootCount.toString(), "Family", Modifier.weight(1f), onRoots)
                    SummaryCard("Elders", storyCount.toString(), "Stories", Modifier.weight(1f), onElders)
                    SummaryCard("Events", eventCount.toString(), "Timeline", Modifier.weight(1f), onTimeline)
                }
            }
            item { FeatureCard("My Family Roots", "Build a living family tree for future generations.", "Open family tree", onRoots) }
            item { FeatureCard("Our Elders Speak", "Save oral history, written stories and voice recordings.", "Open elder stories", onElders) }
            item { FeatureCard("Living Timeline", "Record migrations, milestones, traditions and community moments.", "Open timeline", onTimeline) }
        }
    }

    @Composable
    private fun SummaryCard(title: String, value: String, label: String, modifier: Modifier, onClick: () -> Unit) {
        ElevatedCard(modifier.clickable(onClick = onClick)) {
            Column(Modifier.padding(12.dp)) {
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Forest)
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(label, style = MaterialTheme.typography.labelMedium)
            }
        }
    }

    @Composable
    private fun FeatureCard(title: String, description: String, action: String, onClick: () -> Unit) {
        ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.custodian_heritage),
                    contentDescription = null,
                    modifier = Modifier.size(88.dp).clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(action, color = Forest, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    private fun RootsScreen(roots: SnapshotStateList<RootMember>) {
        var showDialog by remember { mutableStateOf(false) }
        ScreenHeader("My Roots", "Preserve the people and places that connect your family.")
        Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("＋ Add family member") }
        Spacer(Modifier.height(12.dp))
        if (roots.isEmpty()) {
            EmptyState("Your family tree is ready to begin.", "Add a parent, grandparent, ancestor or relative.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(roots) { member ->
                    RecordCard("Family member", member.name, "\${member.relationship} • \${member.community}", if (member.relationship.isBlank()) "" else "Family record saved")
                }
            }
        }
        if (showDialog) {
            EntryDialog(
                title = "Add family member",
                fields = listOf("Name", "Relationship", "Community / village"),
                onDismiss = { showDialog = false },
                onSave = { values ->
                    if (values[0].isNotBlank()) {
                        roots.add(RootMember(values[0], values.getOrElse(1) { "" }, values.getOrElse(2) { "" }))
                        saveRoots(roots)
                    }
                    showDialog = false
                }
            )
        }
    }

    @Composable
    private fun EldersScreen(stories: SnapshotStateList<ElderStory>) {
        var showDialog by remember { mutableStateOf(false) }
        var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
        var isRecording by remember { mutableStateOf(false) }
        var currentAudio by remember { mutableStateOf<String?>(null) }
        val context = LocalContext.current

        ScreenHeader("Our Elders Speak", "Keep oral history alive with written and recorded memories.")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { showDialog = true }, modifier = Modifier.weight(1f)) { Text("＋ Add story") }
            OutlinedButton(
                onClick = {
                    if (!isRecording) {
                        val allowed = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        if (!allowed) {
                            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.RECORD_AUDIO), 90)
                        } else {
                            val file = File(context.filesDir, "recordings/elder_\${System.currentTimeMillis()}.m4a")
                            file.parentFile?.mkdirs()
                            recorder = MediaRecorder(context).apply {
                                setAudioSource(MediaRecorder.AudioSource.MIC)
                                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                setOutputFile(file.absolutePath)
                                prepare()
                                start()
                            }
                            currentAudio = file.absolutePath
                            isRecording = true
                        }
                    } else {
                        recorder?.runCatching {
                            stop()
                            release()
                        }
                        recorder = null
                        isRecording = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isRecording) "■ Stop recording" else "● Quick record")
            }
        }
        Spacer(Modifier.height(12.dp))
        if (stories.isEmpty()) {
            EmptyState("No elder stories yet.", "Capture the knowledge of an elder while it is still available.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(stories) { story ->
                    RecordCard(
                        "Elder story",
                        story.title,
                        "\${story.elder} • \${story.community}",
                        if (story.audioPath != null) "Voice recording saved" else "Written story saved"
                    )
                }
            }
        }
        if (showDialog) {
            EntryDialog(
                title = "New elder story",
                fields = listOf("Elder's name", "Story title", "Category", "Community / village", "Written story / transcript"),
                multilineIndices = setOf(4),
                onDismiss = { showDialog = false },
                onSave = { values ->
                    if (values[0].isNotBlank() && values[1].isNotBlank()) {
                        stories.add(
                            ElderStory(
                                elder = values[0],
                                title = values[1],
                                category = values.getOrElse(2) { "" },
                                community = values.getOrElse(3) { "" },
                                transcript = values.getOrElse(4) { "" },
                                audioPath = currentAudio
                            )
                        )
                        saveStories(stories)
                    }
                    showDialog = false
                }
            )
        }
    }

    @Composable
    private fun TimelineScreen(events: SnapshotStateList<TimelineEvent>) {
        var showDialog by remember { mutableStateOf(false) }
        ScreenHeader("Timeline", "Turn important moments into a history your family can revisit.")
        Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("＋ Add timeline event") }
        Spacer(Modifier.height(12.dp))
        if (events.isEmpty()) {
            EmptyState("Your timeline is empty.", "Add a birth, migration, ceremony, family milestone or community event.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(events) { event ->
                    RecordCard("Timeline event", event.title, event.date, event.description)
                }
            }
        }
        if (showDialog) {
            EntryDialog(
                title = "Add timeline event",
                fields = listOf("Event title", "Date", "Description"),
                multilineIndices = setOf(2),
                onDismiss = { showDialog = false },
                onSave = { values ->
                    if (values[0].isNotBlank()) {
                        events.add(TimelineEvent(values[0], values.getOrElse(1) { "" }, values.getOrElse(2) { "" }))
                        saveEvents(events)
                    }
                    showDialog = false
                }
            )
        }
    }

    @Composable
    private fun ArchiveScreen(
        roots: SnapshotStateList<RootMember>,
        stories: SnapshotStateList<ElderStory>,
        events: SnapshotStateList<TimelineEvent>
    ) {
        val context = LocalContext.current
        var status by remember { mutableStateOf("") }
        val createDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri?.let {
                val payload = backupJson(roots, stories, events, prefs)
                context.contentResolver.openOutputStream(it)?.use { out ->
                    out.write(payload.toString(2).toByteArray())
                }
                status = "Backup saved."
            }
        }
        val openDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                runCatching {
                    val text = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() } ?: ""
                    restoreJson(text, roots, stories, events)
                    status = "Backup restored."
                }.onFailure { status = "Could not restore this backup." }
            }
        }

        ScreenHeader("Archive", "Keep your heritage data safe and portable.")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { createDocument.launch("custodian-backup.json") }, modifier = Modifier.weight(1f)) { Text("Backup") }
            OutlinedButton(onClick = { openDocument.launch(arrayOf("application/json", "text/plain")) }, modifier = Modifier.weight(1f)) { Text("Restore") }
        }
        Spacer(Modifier.height(12.dp))
        ElevatedCard(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.custodian_heritage),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(18.dp))
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Custodian Archive", fontWeight = FontWeight.Bold)
                    Text("\${roots.size} family records • \${stories.size} elder stories • \${events.size} timeline events")
                }
            }
        }
        if (status.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(status, color = Forest, fontWeight = FontWeight.Bold)
        }
    }

    @Composable
    private fun ScreenHeader(title: String, subtitle: String) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Forest)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
        }
    }

    @Composable
    private fun EmptyState(title: String, subtitle: String) {
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.custodian_heritage),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp).clip(RoundedCornerShape(24.dp))
                )
                Spacer(Modifier.height(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    @Composable
    private fun RecordCard(
        kind: String,
        title: String,
        subtitle: String,
        extra: String
    ) {
        ElevatedCard(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.custodian_heritage),
                    contentDescription = null,
                    modifier = Modifier.size(74.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(kind, style = MaterialTheme.typography.labelMedium, color = Forest)
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium)
                    if (extra.isNotBlank()) Text(extra, style = MaterialTheme.typography.labelMedium, color = Gold)
                }
            }
        }
    }

    @Composable
    private fun EntryDialog(
        title: String,
        fields: List<String>,
        multilineIndices: Set<Int> = emptySet(),
        onDismiss: () -> Unit,
        onSave: (List<String>) -> Unit
    ) {
        val values = remember { fields.map { mutableStateOf("") } }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    fields.forEachIndexed { index, label ->
                        OutlinedTextField(
                            value = values[index].value,
                            onValueChange = { values[index].value = it },
                            label = { Text(label) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            minLines = if (index in multilineIndices) 4 else 1,
                            singleLine = index !in multilineIndices
                        )
                    }
                }
            },
            confirmButton = { Button(onClick = { onSave(values.map { it.value }) }) { Text("Save") } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
        )
    }

    private fun saveRoots(data: List<RootMember>) {
        prefs.edit().putStringSet(
            "roots",
            data.map { "\${it.name}||\${it.relationship}||\${it.community}" }.toSet()
        ).apply()
    }

    private fun loadRoots(): List<RootMember> =
        prefs.getStringSet("roots", emptySet()).orEmpty().mapNotNull {
            val p = it.split("||", limit = 3)
            if (p.size == 3) RootMember(p[0], p[1], p[2]) else null
        }

    private fun saveStories(data: List<ElderStory>) {
        prefs.edit().putStringSet(
            "stories",
            data.map {
                listOf(it.elder, it.title, it.category, it.community, it.transcript, it.audioPath ?: "")
                    .joinToString("||")
            }.toSet()
        ).apply()
    }

    private fun loadStories(): List<ElderStory> =
        prefs.getStringSet("stories", emptySet()).orEmpty().mapNotNull {
            val p = it.split("||", limit = 6)
            if (p.size >= 5) ElderStory(
                p[0], p[1], p[2], p[3], p[4], p.getOrNull(5)?.ifBlank { null }
            ) else null
        }

    private fun saveEvents(data: List<TimelineEvent>) {
        prefs.edit().putStringSet("events", data.map { "\${it.title}||\${it.date}||\${it.description}" }.toSet()).apply()
    }

    private fun loadEvents(): List<TimelineEvent> =
        prefs.getStringSet("events", emptySet()).orEmpty().mapNotNull {
            val p = it.split("||", limit = 3)
            if (p.size == 3) TimelineEvent(p[0], p[1], p[2]) else null
        }

    private fun backupJson(
        roots: List<RootMember>,
        stories: List<ElderStory>,
        events: List<TimelineEvent>,
        prefs: android.content.SharedPreferences
    ): JSONObject {
        val rootArray = JSONArray()
        roots.forEach { rootArray.put(JSONObject().apply { put("name", it.name); put("relationship", it.relationship); put("community", it.community) }) }
        val storyArray = JSONArray()
        stories.forEach {
            storyArray.put(JSONObject().apply {
                put("elder", it.elder); put("title", it.title); put("category", it.category)
                put("community", it.community); put("transcript", it.transcript); put("audioPath", it.audioPath ?: "")
            })
        }
        val eventArray = JSONArray()
        events.forEach { eventArray.put(JSONObject().apply { put("title", it.title); put("date", it.date); put("description", it.description) }) }
        return JSONObject().apply {
            put("app", "Custodian")
            put("version", 1)
            put("exportedAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
            put("userName", prefs.getString("user_name", ""))
            put("community", prefs.getString("community", ""))
            put("roots", rootArray)
            put("stories", storyArray)
            put("events", eventArray)
        }
    }

    private fun restoreJson(
        raw: String,
        roots: SnapshotStateList<RootMember>,
        stories: SnapshotStateList<ElderStory>,
        events: SnapshotStateList<TimelineEvent>
    ) {
        val json = JSONObject(raw)
        roots.clear()
        stories.clear()
        events.clear()
        val rootArray = json.optJSONArray("roots") ?: JSONArray()
        for (i in 0 until rootArray.length()) {
            val o = rootArray.getJSONObject(i)
            roots.add(RootMember(o.optString("name"), o.optString("relationship"), o.optString("community")))
        }
        val storyArray = json.optJSONArray("stories") ?: JSONArray()
        for (i in 0 until storyArray.length()) {
            val o = storyArray.getJSONObject(i)
            stories.add(
                ElderStory(
                    o.optString("elder"),
                    o.optString("title"),
                    o.optString("category"),
                    o.optString("community"),
                    o.optString("transcript"),
                    o.optString("audioPath").ifBlank { null }
                )
            )
        }
        val eventArray = json.optJSONArray("events") ?: JSONArray()
        for (i in 0 until eventArray.length()) {
            val o = eventArray.getJSONObject(i)
            events.add(TimelineEvent(o.optString("title"), o.optString("date"), o.optString("description")))
        }
        saveRoots(roots)
        saveStories(stories)
        saveEvents(events)
    }
}

private fun Color.toArgb(): Int =
    android.graphics.Color.argb((alpha * 255).toInt(), (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())

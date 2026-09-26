package com.custodian.android

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SafetyState(
    val completedSteps: Set<Int> = emptySet(),
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val lastCheckIn: String = "",
    val savedStories: Set<String> = emptySet(), val profileName: String = "", val profileAbout: String = "", val userStories: List<String> = emptyList()
)

class SafetyRepository(context: Context) {
    private val preferences = context.getSharedPreferences("custodian_safety", Context.MODE_PRIVATE)

    fun load(): SafetyState {
        val completed = mutableSetOf<Int>()
        val array = JSONArray(preferences.getString("completed_steps", "[]"))
        for (i in 0 until array.length()) completed += array.getInt(i)

        return SafetyState(
            completedSteps = completed,
            emergencyContactName = preferences.getString("contact_name", "") ?: "",
            emergencyContactPhone = preferences.getString("contact_phone", "") ?: "",
            lastCheckIn = preferences.getString("last_check_in", "") ?: "",
            savedStories = preferences.getStringSet("saved_stories", emptySet()) ?: emptySet(), profileName = preferences.getString("profile_name", "") ?: "", profileAbout = preferences.getString("profile_about", "") ?: "", userStories = loadUserStories()
        )
    }

    fun save(state: SafetyState) {
        val array = JSONArray()
        state.completedSteps.sorted().forEach { array.put(it) }

        preferences.edit()
            .putString("completed_steps", array.toString())
            .putString("contact_name", state.emergencyContactName)
            .putString("contact_phone", state.emergencyContactPhone)
            .putString("last_check_in", state.lastCheckIn)
            .putStringSet("saved_stories", state.savedStories)
            .putString("profile_name", state.profileName)
            .putString("profile_about", state.profileAbout)
            .putString("user_stories", JSONArray(state.userStories).toString())
            .apply()
    }
    private fun loadUserStories(): List<String> {
        val array = JSONArray(preferences.getString("user_stories", "[]"))
        return List(array.length()) { array.getString(it) }
    }
}

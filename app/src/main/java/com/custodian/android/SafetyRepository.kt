package com.custodian.android

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SafetyState(
    val completedSteps: Set<Int> = emptySet(),
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val lastCheckIn: String = "",
    val checkInHistory: List<String> = emptyList(),
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
            checkInHistory = loadCheckInHistory(),
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
            .putString("check_in_history", JSONArray(state.checkInHistory).toString())
            .putStringSet("saved_stories", state.savedStories)
            .putString("profile_name", state.profileName)
            .putString("profile_about", state.profileAbout)
            .putString("user_stories", JSONArray(state.userStories).toString())
            .apply()
    }
    private fun loadCheckInHistory(): List<String> {
        val array = JSONArray(preferences.getString("check_in_history", "[]"))
        return List(array.length()) { array.getString(it) }
    }

    private fun loadUserStories(): List<String> {
        val array = JSONArray(preferences.getString("user_stories", "[]"))
        return List(array.length()) { array.getString(it) }
    }
}

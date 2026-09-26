package com.custodian.android

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SafetyState(
    val completedSteps: Set<Int> = emptySet(),
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val lastCheckIn: String = ""
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
            lastCheckIn = preferences.getString("last_check_in", "") ?: ""
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
            .apply()
    }
}

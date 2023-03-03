package com.wilsontryingapp2023.slidegame

import android.content.Context
import android.os.Bundle
import android.preference.ListPreference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.preference.SwitchPreference


class Prefs : PreferenceActivity() {
    public override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val ps = preferenceManager.createPreferenceScreen(this)
        val music = SwitchPreference(this)
        music.setTitle(R.string.music_question)
        music.setSummaryOn(R.string.yes)
        music.setSummaryOff(R.string.no)
        music.setDefaultValue(true)
        music.key = "MUSIC_PREF"
        val ls = ListPreference(this)
        ls.setTitle(R.string.speed)
        ls.setSummary(R.string.speed_question)
        ls.key = "SPEED_PREF"
        ls.setEntries(arrayOf(resources.getString(R.string.fast), resources.getString(R.string.normal), resources.getString(R.string.slow)))
        val values = arrayOf("50", "100", "200")
        ls.entryValues = values
        ls.setDefaultValue("100")

        // add prefs
        ps.addPreference(music)
        ps.addPreference(ls)
        preferenceScreen = ps
    }

    companion object {
        fun soundOn(c: Context?): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(c).getBoolean("MUSIC_PREF", true)
        }

        fun getSpeed(c: Context?): Int {
            return PreferenceManager.getDefaultSharedPreferences(c).getString("SPEED_PREF", "100")!!
                .toInt()
        }
    }
}

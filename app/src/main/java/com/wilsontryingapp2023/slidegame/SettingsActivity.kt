package com.wilsontryingapp2023.slidegame

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference

class SettingsActivity : AppCompatActivity() {

    companion object {
        fun soundOn(context: Context?): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context!!).getBoolean("MUSIC_PREF", true)
        }

        fun getSpeed(context: Context?): Int {
            return PreferenceManager.getDefaultSharedPreferences(context!!).getString("SPEED_PREF", "100")!!.toInt()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            val context = preferenceManager.context
            val screen = preferenceManager.createPreferenceScreen(context)

            val music = SwitchPreference(context)
            music.setTitle(R.string.music_question)
            music.setSummaryOn(R.string.yes)
            music.setSummaryOff(R.string.no)
            music.setDefaultValue(true)
            music.key = "MUSIC_PREF"
            screen.addPreference(music)

            val ls = ListPreference(context)
            ls.setTitle(R.string.speed)
            ls.setSummary(R.string.speed_question)
            ls.key = "SPEED_PREF"
            ls.entries = arrayOf(resources.getString(R.string.fast), resources.getString(R.string.normal), resources.getString(R.string.slow))
            val values = arrayOf("50", "100", "200")
            ls.entryValues = values
            ls.setDefaultValue("100")
            screen.addPreference(ls)

            preferenceScreen = screen
        }
    }
}
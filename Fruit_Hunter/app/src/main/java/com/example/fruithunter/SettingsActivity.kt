package com.example.fruithunter

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.example.fruithunter.BackgroundMusicService

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, SettingsFragment()).commit()

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

        private var backgroundMusicService: BackgroundMusicService? = null
        private var isServiceBound = true

        private val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as BackgroundMusicService.LocalBinder
                backgroundMusicService = binder.getService()
                isServiceBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                isServiceBound = false
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val musicEnabledSwitch = findPreference<SwitchPreference>("music_enabled")
            val selectedMusicList = findPreference<ListPreference>("selected_music")

            musicEnabledSwitch?.setOnPreferenceChangeListener { _, newValue ->
                val musicEnabled = newValue as Boolean
                updateMusicState(musicEnabled)
                true
            }

            selectedMusicList?.setOnPreferenceChangeListener { _, newValue ->
                val selectedMusicKey = newValue as String
                updateSelectedMusic(selectedMusicKey)
                true
            }

            // Настройка слушателя для кнопки "Назад"
            findPreference<Preference>("back_button")?.setOnPreferenceClickListener {
                requireActivity().onBackPressed()
                true
            }
        }

        private fun updateMusicState(isEnabled: Boolean) {
            if (isServiceBound) {
                if (isEnabled) {
                    backgroundMusicService?.startMusic()
                } else {
                    backgroundMusicService?.stopMusic()
                }
            }
        }

        private fun updateSelectedMusic(selectedMusicKey: String) {
            if (isServiceBound) {
                val resourceId = getMusicResourceId(selectedMusicKey)
                backgroundMusicService?.setMusicSource(resourceId)
            }
        }

        private fun bindBackgroundMusicService() {
            val serviceIntent = Intent(requireContext(), BackgroundMusicService::class.java)
            requireActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        private fun unbindBackgroundMusicService() {
            if (isServiceBound) {
                requireActivity().unbindService(serviceConnection)
                isServiceBound = false
            }
        }

        override fun onResume() {
            super.onResume()
            preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
            bindBackgroundMusicService()
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
            unbindBackgroundMusicService()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            // Handle preference changes if needed
        }

        private fun getMusicResourceId(key: String): Int {
            return when (key) {
                "music1" -> R.raw.music1
                "music2" -> R.raw.music2
                "music3" -> R.raw.music3
                // Add other music options as needed
                else -> R.raw.music1
            }
        }
    }
}
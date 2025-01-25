package com.ushastoe.stfu

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.DynamicColors
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    private val PREFERENCES_NAME = "NotifyControlPrefs"
    private val PACKAGES_KEY = "PackageNames"
    private val ENABLE_FUNC = "Enable"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this@MainActivity)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (!isNotificationAccessGranted()) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Разрешите доступ к уведомлениям для этого приложения", Toast.LENGTH_LONG).show()
        }

        val buttonShowDialog: Button = findViewById(R.id.buttonShowDialog)
        buttonShowDialog.setOnClickListener { showAppListDialog() }
        val switchEnabler: MaterialSwitch = findViewById(R.id.switchEnaler)
        switchEnabler.isChecked = getEnabledFunc()
        switchEnabler.setOnCheckedChangeListener { _, isChecked ->
            setEnabledFunc(isChecked)
        }

    }

    private fun isNotificationAccessGranted(): Boolean {
        val packageName = packageName
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledListeners != null && enabledListeners.contains(packageName)
    }


    private fun showAppListDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_app_list)
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        val recyclerView: RecyclerView = dialog.findViewById(R.id.recyclerViewApps)
        val editTextSearch: EditText = dialog.findViewById(R.id.editText)
        val progressBar: ProgressBar = dialog.findViewById(R.id.progressBarLoading)
        val buttonSave: Button = dialog.findViewById(R.id.buttonSave)
        val buttonCancel: Button = dialog.findViewById(R.id.buttonCancel)

        val selectedPackages = getSavedPackages().toMutableSet()

        // Показываем ProgressBar и начинаем загрузку
        progressBar.visibility = ProgressBar.VISIBLE
        recyclerView.visibility = RecyclerView.GONE

        // Загружаем список приложений асинхронно
        CoroutineScope(Dispatchers.Main).launch {
            // Загрузка приложений в фоновом потоке
            val apps = withContext(Dispatchers.IO) { getInstalledApps() }

            // Разделяем приложения на выбранные и невыбранные
            val (selectedApps, unselectedApps) = apps.partition { selectedPackages.contains(it.packageName) }
            // Объединяем два списка: сначала выбранные, затем невыбранные
            val selectedAppList = (selectedApps).sortedBy { it.appName.lowercase() }.toMutableList()
            val unSelectedAppList = (unselectedApps).sortedBy { it.appName.lowercase() }.toMutableList()
            val appList = (selectedAppList + unSelectedAppList).toMutableList()


            val adapter = AppListAdapter(appList) { app, isSelected ->
                if (isSelected) {
                    selectedPackages.add(app.packageName)
                } else {
                    selectedPackages.remove(app.packageName)
                }
            }

            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            recyclerView.adapter = adapter

            // Скрываем ProgressBar после загрузки
            progressBar.visibility = ProgressBar.GONE
            recyclerView.visibility = RecyclerView.VISIBLE

            // Обработка ввода текста для фильтрации
            editTextSearch.addTextChangedListener { text ->
                val query = text.toString()
                adapter.filter(query) // Обновление фильтрации в адаптере
            }
        }

        buttonSave.setOnClickListener {
            // Получаем только выбранные приложения
            val selectedApps = (recyclerView.adapter as AppListAdapter).getSelectedApps()

            // Сохраняем только пакеты выбранных приложений
            savePackages(selectedApps.map { it.packageName })  // Сохраняем в SharedPreferences

            dialog.dismiss() // Закрываем диалог
        }


        // Кнопка "Отмена"
        buttonCancel.setOnClickListener {
            dialog.dismiss() // Просто закрыть диалог
        }

        dialog.show()
    }



    private fun getInstalledApps(): List<AppInfo> {
        val pm: PackageManager = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps
            .map {
                AppInfo(
                    appName = pm.getApplicationLabel(it).toString(),
                    packageName = it.packageName,
                    icon = pm.getApplicationIcon(it),
                    isSelected = getSavedPackages().contains(it.packageName)
                )
            }
            .sortedBy { it.appName.lowercase() } // Сортировка по имени в алфавитном порядке
    }




    private fun getSavedPackages(): List<String> {
        val sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val savedPackages = sharedPreferences.getString(PACKAGES_KEY, "") ?: ""
        return savedPackages.split(";").filter { it.isNotEmpty() }
    }

    private fun savePackages(packages: List<String>) {
        val sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val packagesString = packages.joinToString(";")
        sharedPreferences.edit().putString(PACKAGES_KEY, packagesString).apply()
    }

    private fun getEnabledFunc(): Boolean {
        val sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val enabled = sharedPreferences.getBoolean(ENABLE_FUNC, false)
        return enabled
    }

    private fun setEnabledFunc(enabled: Boolean) {
        val sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(ENABLE_FUNC, enabled).apply()
    }
}
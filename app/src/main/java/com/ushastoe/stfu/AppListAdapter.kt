package com.ushastoe.stfu

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    var isSelected: Boolean = false
)
class AppListAdapter(
    private var apps: MutableList<AppInfo>,
    private val onAppSelected: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    private val originalList: List<AppInfo> = apps.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app)
    }

    override fun getItemCount(): Int = apps.size

    fun getSelectedApps(): List<AppInfo> {
        return apps.filter { it.isSelected }
    }

    // Метод для фильтрации списка
    fun filter(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter { it.appName.contains(query, ignoreCase = true) }
        }
        apps.clear()
        apps.addAll(filteredList)
        notifyDataSetChanged()
    }

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appName: TextView = itemView.findViewById(R.id.textViewAppName)
        private val appIcon: ImageView = itemView.findViewById(R.id.imageViewAppIcon)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxApp)

        fun bind(app: AppInfo) {
            appName.text = app.appName
            appIcon.setImageDrawable(app.icon)
            checkBox.isChecked = app.isSelected

            // Обрабатываем изменение состояния чекбокса
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                app.isSelected = isChecked
                onAppSelected(app, isChecked)  // Уведомляем о том, что состояние чекбокса изменилось
            }

            itemView.setOnClickListener {
                // Инвертируем состояние чекбокса при клике
                checkBox.isChecked = !checkBox.isChecked
            }
        }
    }

}

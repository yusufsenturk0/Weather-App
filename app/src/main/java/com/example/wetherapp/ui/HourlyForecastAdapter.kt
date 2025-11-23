package com.example.wetherapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.example.wetherapp.R
import com.example.wetherapp.data.model.ForecastItem
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

class HourlyForecastAdapter(private var hourlyList: List<ForecastItem>) :
    RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>() {

    class HourlyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val ivWeatherIcon: ImageView = view.findViewById(R.id.ivWeatherIcon)
        val tvTemp: TextView = view.findViewById(R.id.tvTemp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_forecast, parent, false)
        return HourlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        val item = hourlyList[position]

        // Format time (e.g., "15:00")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        // Debug log
        android.util.Log.d("HourlyAdapter", "Raw dt_txt: ${item.dt_txt}")

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val dateTime = java.time.LocalDateTime.parse(item.dt_txt, formatter)
                holder.tvTime.text = dateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            } else {
                // Fallback for older SDKs (though minSdk is 26)
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = inputFormat.parse(item.dt_txt)
                holder.tvTime.text = date?.let { outputFormat.format(it) } ?: item.dt_txt
            }
        } catch (e: Exception) {
            // Fallback to simple substring if parsing fails
            if (item.dt_txt.length >= 16) {
                holder.tvTime.text = item.dt_txt.substring(11, 16)
            } else {
                holder.tvTime.text = item.dt_txt
            }
        }

        holder.tvTemp.text = "${item.main.temp.roundToInt()}°"

        val iconCode = item.weather.firstOrNull()?.icon
        if (iconCode != null) {
            holder.ivWeatherIcon.setImageResource(com.example.wetherapp.util.WeatherIconMapper.getIconResource(iconCode))
        }
    }

    override fun getItemCount() = hourlyList.size

    fun updateData(newList: List<ForecastItem>) {
        hourlyList = newList
        notifyDataSetChanged()
    }
}

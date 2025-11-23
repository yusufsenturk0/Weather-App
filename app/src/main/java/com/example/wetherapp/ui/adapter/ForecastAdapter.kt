package com.example.wetherapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.wetherapp.data.model.ForecastItem
import com.example.wetherapp.databinding.ItemForecastBinding
import com.example.wetherapp.R
import java.text.SimpleDateFormat
import java.util.Locale

class ForecastAdapter(private val onDayClick: (ForecastItem) -> Unit) : ListAdapter<ForecastItem, ForecastAdapter.ForecastViewHolder>(ForecastDiffCallback()) {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ItemForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        
        val isSelected = position == selectedPosition
        holder.itemView.setBackgroundResource(if (isSelected) R.drawable.bg_card_selected else R.drawable.bg_card)

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onDayClick(item)
        }
    }

    fun clearSelection() {
        val previousPosition = selectedPosition
        selectedPosition = RecyclerView.NO_POSITION
        notifyItemChanged(previousPosition)
    }

    class ForecastViewHolder(private val binding: ItemForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ForecastItem) {
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.dt_txt)
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
            binding.tvDay.text = date?.let { dayFormat.format(it) } ?: item.dt_txt

            binding.tvForecastTemp.text = "${item.main.temp.toInt()}°"
            
            val iconCode = item.weather.firstOrNull()?.icon
            if (iconCode != null) {
                val iconUrl = "http://openweathermap.org/img/wn/$iconCode@2x.png"
                binding.ivForecastIcon.load(iconUrl)
            }
        }
    }

    class ForecastDiffCallback : DiffUtil.ItemCallback<ForecastItem>() {
        override fun areItemsTheSame(oldItem: ForecastItem, newItem: ForecastItem): Boolean {
            return oldItem.dt == newItem.dt
        }

        override fun areContentsTheSame(oldItem: ForecastItem, newItem: ForecastItem): Boolean {
            return oldItem == newItem
        }
    }
}

package com.example.awesomephotoframe

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.awesomephotoframe.viewmodel.WeatherViewModel


class MainActivity : AppCompatActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var tvWeather: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvWeather = findViewById(R.id.tv_weather)

        // 天気情報をロード
        weatherViewModel.loadWeather(35.6895, 139.6917)

        // LiveData を監視 (observe)
        weatherViewModel.weather.observe(this, Observer { weather ->
            weather?.let {
                tvWeather.text = "${it.temperature}°C, ${it.description}"
            }
        })
    }
}
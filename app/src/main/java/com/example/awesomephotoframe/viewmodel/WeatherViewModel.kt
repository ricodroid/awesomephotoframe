package com.example.awesomephotoframe.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomephotoframe.data.model.WeatherResponse
import com.example.awesomephotoframe.data.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()

    private val _weather = MutableLiveData<WeatherResponse>()
    val weather: LiveData<WeatherResponse> = _weather

    fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            val weatherData = repository.fetchWeather()
            _weather.postValue(weatherData)
        }
    }
}
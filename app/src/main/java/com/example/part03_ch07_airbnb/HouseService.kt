package com.example.part03_ch07_airbnb

import retrofit2.Call
import retrofit2.http.GET

interface HouseService {

    @GET("/v3/8c6935e7-d049-4e27-9f6c-db5698201792")
    fun getHouseList(): Call<HouseDto>

}
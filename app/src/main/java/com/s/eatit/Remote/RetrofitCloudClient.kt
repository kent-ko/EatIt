package com.s.eatit.Remote

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitCloudClient {
    private var instance: Retrofit? = null

    fun getInstance():Retrofit{
        if(instance == null)
            instance = Retrofit.Builder()
                .baseUrl("https://us-central1-ajiri-8ffc5.cloudfunctions.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        return instance!!
    }
}
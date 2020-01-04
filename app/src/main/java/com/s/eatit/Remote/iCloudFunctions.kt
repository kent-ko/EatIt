package com.s.eatit.Remote

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface iCloudFunctions {
    @GET("getCustomToken")
    fun getCustomToken(@Query("access_token") accessToken: String) : Observable<ResponseBody>
}
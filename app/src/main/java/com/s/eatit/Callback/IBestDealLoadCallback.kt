package com.s.eatit.Callback

import com.s.eatit.Model.BestDealModel

interface IBestDealLoadCallback {
    fun onBestDealLoadSuccess(bestDealList: List<BestDealModel>)
    fun onBestDealLoadFailed(message:String)
}
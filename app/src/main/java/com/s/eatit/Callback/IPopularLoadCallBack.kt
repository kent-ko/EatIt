package com.s.eatit.Callback

import com.s.eatit.Model.PopularCategoryModel

interface IPopularLoadCallBack {
    fun onPopularLoadSuccess(popularModelList:List<PopularCategoryModel>)
    fun onPopularLoadFailed(message:String)
}
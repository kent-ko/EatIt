package com.s.eatit.Callback

import com.s.eatit.Model.CategoryModel

interface ICategoryCallbackListener {

    fun onCategoryLoadSuccess(categoriesList: List<CategoryModel>)
    fun onCategoryLoadFailed(message:String)

}

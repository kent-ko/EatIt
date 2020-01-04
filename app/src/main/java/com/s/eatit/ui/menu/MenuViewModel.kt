package com.s.eatit.ui.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.s.eatit.Callback.ICategoryCallbackListener
import com.s.eatit.Common.Common
import com.s.eatit.Model.CategoryModel

class MenuViewModel : ViewModel(), ICategoryCallbackListener {

        private var categoriesListMutable: MutableLiveData<List<CategoryModel>>?=null
        private var messageError: MutableLiveData<String> = MutableLiveData()
        private val categoryCallBackListener: ICategoryCallbackListener

    init {
        categoryCallBackListener = this
    }

    override fun onCategoryLoadSuccess(categoriesList: List<CategoryModel>) {
        categoriesListMutable!!.value = categoriesList
    }

    override fun onCategoryLoadFailed(message: String) {
        messageError!!.value = message
    }



    fun getCategoryList():MutableLiveData<List<CategoryModel>>{
        if (categoriesListMutable == null){
            categoriesListMutable = MutableLiveData()
            loadCategory()

        }
        return categoriesListMutable!!
    }

    fun getMessageError():MutableLiveData<String>{
        return messageError
    }



    private fun loadCategory() {
        val tempList = ArrayList<CategoryModel>()
        val categoryRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
        categoryRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                categoryCallBackListener.onCategoryLoadFailed(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapShot in p0!!.children)
                {
                    val model = itemSnapShot.getValue<CategoryModel>(CategoryModel::class.java)
                    model!!.menu_id = itemSnapShot.key
                    tempList.add(model!!)
                }
                categoryCallBackListener.onCategoryLoadSuccess(tempList)
            }

        })
    }
}
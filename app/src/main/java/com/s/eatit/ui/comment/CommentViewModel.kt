package com.s.eatit.ui.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.s.eatit.Model.CommentModel

class CommentViewModel : ViewModel() {

    val mutableLiveDataCommentList:MutableLiveData<List<CommentModel>>

    init {
        mutableLiveDataCommentList = MutableLiveData()
    }

    fun setCommentList(commentList:List<CommentModel>)
    {
        mutableLiveDataCommentList.value = commentList
    }

}
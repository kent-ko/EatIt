package com.s.eatit.Callback

import com.s.eatit.Model.CommentModel

interface ICommentCallBack {
    fun onCOmmentLoadSuccess(commentList: List<CommentModel>)
    fun onCommentLoadFailed(message:String)

}

package com.s.eatit.ui.cart

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.internal.service.Common
import com.s.eatit.Database.CartDataSource
import com.s.eatit.Database.CartDatabase
import com.s.eatit.Database.CartItem
import com.s.eatit.Database.LocalCartDataSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CartViewModel : ViewModel() {

   private var compositeDisposable:CompositeDisposable
    private var cartDataSource:CartDataSource?=null
    private var mutableLiveDataCartItem:MutableLiveData<List<CartItem>>?=null

    init {
        compositeDisposable = CompositeDisposable()
    }

    fun initCartDataSource(context: Context){
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }

    fun getMutableLiveDataCartItem() : MutableLiveData<List<CartItem>>{
        if (mutableLiveDataCartItem == null)
        {
            mutableLiveDataCartItem = MutableLiveData()
        }
        getCartItems()

        return mutableLiveDataCartItem!!
    }

    private fun getCartItems(){

        compositeDisposable.addAll(cartDataSource!!.getAllCart(com.s.eatit.Common.Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({cartItem ->
                mutableLiveDataCartItem!!.value = cartItem
            }, {
                    t: Throwable? -> mutableLiveDataCartItem!!.value = null }))
    }

    fun onStop(){
        compositeDisposable.clear()
    }

}

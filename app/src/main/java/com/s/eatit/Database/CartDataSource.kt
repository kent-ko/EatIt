package com.s.eatit.Database

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface CartDataSource {
    fun getAllCart(uid: String): Flowable<List<CartItem>>

    fun countItemInCart(uid: String): Single<Int>

    fun sumPrice(uid: String): Single<Long>

    fun getItemInCart(foodId: String, uid: String): Single<CartItem>

    fun insertOrReplaceAll(vararg cartItems: CartItem): Completable

    fun updateCart(cart: CartItem): Single<Int>

    fun deleteCart(cart: CartItem): Single<Int>

    fun cleanCart(uid: String): Single<Int>

}
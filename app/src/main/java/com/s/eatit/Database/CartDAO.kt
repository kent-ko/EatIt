package com.s.eatit.Database

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface CartDAO {

    @androidx.room.Query("SELECT * FROM Cart WHERE uid=:uid")
    fun getAllCart(uid: String): Flowable<List<CartItem>>

    @androidx.room.Query("SELECT COUNT(*) FROM Cart WHERE uid=:uid")
    fun countItemInCart(uid: String): Single<Int>

    @androidx.room.Query("SELECT SUM(foodQuantity * foodPrice) + (foodExtraPrice*foodQuantity) FROM Cart WHERE uid=:uid")
    fun sumPrice(uid: String): Single<Long>

    @androidx.room.Query("SELECT * FROM Cart WHERE foodId=:foodId AND uid=:uid")
    fun getItemInCart(foodId: String, uid: String): Single<CartItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceAll(vararg cartItems: CartItem): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateCart(cart: CartItem): Single<Int>

    @Delete
    fun deleteCart(cart: CartItem): Single<Int>

    @androidx.room.Query("DELETE FROM Cart WHERE uid=:uid")
    fun cleanCart(uid: String): Single<Int>
}
package com.s.eatit.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.s.eatit.Database.CartDataSource
import com.s.eatit.Database.CartDatabase
import com.s.eatit.Database.CartItem
import com.s.eatit.Database.LocalCartDataSource
import com.s.eatit.EventBus.UpdateItemCart
import com.s.eatit.R
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

class MyCartAdapter(internal var context: Context,
                    internal var cartItem: List<CartItem>) : RecyclerView.Adapter<MyCartAdapter.MyViewHolder>() {


    internal var compositeDisposable: CompositeDisposable
    internal var cartDataSource: CartDataSource

    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_cart_item, parent, false))
    }

    override fun getItemCount(): Int {
        return cartItem.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        Glide.with(context).load(cartItem.get(position).foodImage).into(holder.img_cart)
        holder.txt_food_name.text = StringBuilder(cartItem.get(position).foodName!!)
        holder.txt_food_price.text = StringBuilder("").append(cartItem[position].foodPrice + cartItem[position].foodExtraPrice)
        holder.number_button.number = cartItem[position].foodQuantity.toString()

        //even
        holder.number_button.setOnValueChangeListener{view, oldValue, newValue ->
            cartItem[position].foodQuantity = newValue
            EventBus.getDefault().postSticky(UpdateItemCart(cartItem[position]))
        }

    }

    fun getItemAtPosition(pos: Int): CartItem {
        return cartItem[pos]
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            lateinit var img_cart: ImageView
            lateinit var txt_food_name: TextView
            lateinit var txt_food_price: TextView
            lateinit var number_button: ElegantNumberButton

            init {
                img_cart = itemView.findViewById(R.id.img_cart) as ImageView
                txt_food_name = itemView.findViewById(R.id.txt_cart_food_name) as TextView
                txt_food_price = itemView.findViewById(R.id.txt_cart_foodPrice) as TextView
                number_button = itemView.findViewById(R.id.cart_number_button) as ElegantNumberButton

            }

        }
}
package com.s.eatit.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.asksira.loopingviewpager.LoopingPagerAdapter
import com.bumptech.glide.Glide
import com.s.eatit.Model.BestDealModel
import com.s.eatit.R

class MyBestDealsAdapter(context:Context,
                         itemList:List<BestDealModel>,
                         isInfinite:Boolean):
    LoopingPagerAdapter<BestDealModel>(context, itemList, isInfinite) {

    override fun inflateView(viewType: Int, container: ViewGroup?, listPosition: Int): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_best_deals_items, container, false)
    }

    override fun bindView(convertView: View?, listPosition: Int, viewType: Int) {
        val  imageView = convertView!!.findViewById<ImageView>(R.id.img_best_deals)
        val  textView = convertView!!.findViewById<TextView>(R.id.txt_best_deals)

        //set data
        Glide.with(context).load(itemList[listPosition].image).into(imageView)
        textView.text = itemList[listPosition].name
    }

}
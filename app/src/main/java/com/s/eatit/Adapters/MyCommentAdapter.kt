package com.s.eatit.Adapters

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.s.eatit.Model.CommentModel
import com.s.eatit.R
import kotlinx.android.synthetic.main.layout_comment_item.view.*
import java.sql.Timestamp

class MyCommentAdapter(internal var context: Context,
                       internal var commentList: List<CommentModel>) : RecyclerView.Adapter<MyCommentAdapter.MyViewHolder>(){



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_comment_item, parent, false))
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val timestamp = commentList.get(position).commentTimeStamp!!["timestamp"]!!.toString().toLong()
        holder.date!!.text = DateUtils.getRelativeTimeSpanString(timestamp)
        holder.name!!.text = commentList.get(position).name
        holder.comment!!.text = commentList.get(position).comment
        holder.ratingBar!!.rating = commentList.get(position).ratingValue
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        var img_acc: ImageView?=null
        var name:TextView?=null
        var comment:TextView?=null
        var date:TextView?=null
        var ratingBar:RatingBar?=null

        init{
            comment = itemView.findViewById(R.id.txt_comment) as TextView
            name = itemView.findViewById(R.id.txt_comment_name) as TextView
            date = itemView.findViewById(R.id.txt_comment_date) as TextView
            ratingBar = itemView.findViewById(R.id.rating_Bar) as RatingBar

        }
    }
}

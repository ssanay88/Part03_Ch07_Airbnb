package com.example.part03_ch07_airbnb

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

// BottomSheet에 들어가는 recyclerView 어댑터
class HouseListAdapter: ListAdapter<HouseModel,HouseListAdapter.ItemViewHolder>(diff) {

    inner class ItemViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        fun bind(houseModel:HouseModel) {
            val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
            val priceTextView = view.findViewById<TextView>(R.id.priceTextView)
            val thumbnailImageView = view.findViewById<ImageView>(R.id.thumbnailImageView)

            titleTextView.text = houseModel.title
            priceTextView.text = houseModel.price

            Glide.with(thumbnailImageView.context)
                .load(houseModel.imgUrl)
                .transform(CenterCrop(),RoundedCorners(dpToPx(thumbnailImageView.context, 12)))     // 이미지를 변경하는 설정 , CenterCrop:가로/세로 중 길이가 짧은 쪽에 맞춰 이미지 크기를 변경해서 출력, 나머지는 삭제
                .into(thumbnailImageView)

        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HouseListAdapter.ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // inflate된 뷰가 ItemViewHolder의 view로 들어간다.
        return ItemViewHolder(inflater.inflate(R.layout.item_house, parent, false))
    }

    override fun onBindViewHolder(holder: HouseListAdapter.ItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    // Glide의 RoundedCorners에는 픽셀값이 들어가야한다. 하지만 우리가 입력하는 값은 DP기준이므로 입력한 DP를 픽셀로 바꿔서 앞의 함수에 넣어줘야한다.
    private fun dpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
    }

    companion object {
        val diff = object : DiffUtil.ItemCallback<HouseModel>() {
            override fun areItemsTheSame(oldItem: HouseModel, newItem: HouseModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: HouseModel, newItem: HouseModel): Boolean{
                return oldItem == newItem
            }

        }
    }


}
package com.afivadnan.y

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView

class RvAdapter(
    private val onItemClick: (position: Int, data: Friend) -> Unit
) : RecyclerView.Adapter<RvAdapter.FriendViewHolder>() {

    private var listItem = emptyList<Friend>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_teman, parent, false)
        return FriendViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = listItem[position]
        holder.nama.text = item.name
        holder.sekolah.text = item.school
        holder.hobi.text = item.hobby

        val photoBitmap = AddFriend().strToBitmap(item.photo)
        if (photoBitmap != null) {
            holder.pto.setImageBitmap(photoBitmap)
        } else {
            Log.d("RvAdapter", "Image is null for position: $position")
            holder.pto.setImageResource(R.drawable.baseline_auto_mode_24)
        }

        holder.itemView.setOnClickListener { onItemClick(position, item) }
    }


    override fun getItemCount(): Int {
        return listItem.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<Friend>) {
        this.listItem = list
        notifyDataSetChanged()
    }

    class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nama: TextView = view.findViewById(R.id.nama)
        val sekolah: TextView = view.findViewById(R.id.sekolah)
        val hobi: TextView = view.findViewById(R.id.hobi)
        val pto: ImageView = view.findViewById(R.id.Image)
    }
}

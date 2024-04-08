package com.example.musicplayer

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso


class AdapterMusicCover(private val musicList:List<Music>,private val context: Context) : RecyclerView.Adapter<AdapterMusicCover.ViewHolder>(){

    private val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    private val density = displayMetrics.density

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val screenWidth = displayMetrics.widthPixels
        val desiredWidth = screenWidth - (22 * density)

        val view = LayoutInflater.from(parent.context).inflate(R.layout.music_cover, parent, false)
        val layoutParams = view.layoutParams

        layoutParams.width = desiredWidth.toInt()
        view.layoutParams = layoutParams
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.get().load(musicList[position].musicCoverURL).into(holder.coverImage)
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val containerCover: RelativeLayout = itemView.findViewById(R.id.container_musicCover)
        val coverImage: ImageView = itemView.findViewById(R.id.cover_image)
    }

}
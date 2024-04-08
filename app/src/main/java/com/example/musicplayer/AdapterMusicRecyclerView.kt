package com.example.musicplayer

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import java.util.ArrayList


class AdapterMusicRecyclerView(private val musicList:List<Music>, private val context:Context,
                               private val launcher: ActivityResultLauncher<Intent>) : RecyclerView.Adapter<AdapterMusicRecyclerView.ViewHolder>(){

    companion object {
        val LIVE_POSITION_DATA = MutableLiveData<Int?>(null)
    }

    private var prevImageButton:ImageButton? = null
    private var prevContainer:View? = null
    private var selectedPosition:LiveData<Int?>  = LIVE_POSITION_DATA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.music_item, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Picasso.get().load(musicList[position].musicCoverURL).into(holder.coverImage)
        holder.musicName.text = musicList[position].music_name
        holder.musicArtist.text = musicList[position].artist

        holder.container.setOnClickListener{

            LIVE_POSITION_DATA.value = holder.adapterPosition
            launchActivity()
        }

        holder.btnPlayPause.setOnClickListener{btn->
            if(!btn.isSelected)
            {
                btn.isSelected = true
                myMediaPlayer.pauseMusic()
            }
            else{
                btn.isSelected = false
                myMediaPlayer.resumeMusic()
            }
        }

        addObserver(holder.coverImage,holder.container,holder.btnPlayPause,holder.adapterPosition)
    }

    private fun setDominantColor(imageView: ShapeableImageView,container:RelativeLayout) {

        val bitmap = imageView.drawable.toBitmap()

        Palette.from(bitmap).generate {
            it?.getDarkVibrantColor(0x000000)?.let { color ->

                val newAlpha = (color * (225 * 0.45)).toInt() // 45% transparency
                val newColor = (newAlpha shl 24) or (color and 0x00FFFFFF)
                container.setBackgroundColor(newColor)
            }
        }
    }


    private fun addObserver(imageView: ShapeableImageView,container:View,btnPlayPause:ImageButton,position: Int)
    {
        selectedPosition.observe(context as LifecycleOwner){it1->

            it1?.let {
                if(position == LIVE_POSITION_DATA.value) {

                    btnPlayPause.let {
                        if(it.visibility == View.INVISIBLE) {
                            it.visibility = View.VISIBLE

                            prevImageButton = prevImageButton?.let {prevIt->
                                prevIt.isSelected = false
                                prevIt.visibility = View.INVISIBLE
                                it
                            }?: it
                        }
                    }
                    btnPlayPause.isSelected = myMediaPlayer.mediaPlayer.isPlaying != true

                    setDominantColor(imageView, container as RelativeLayout)
                    prevContainer = prevContainer?.let {it3->
                        it3.setBackgroundColor(0x000000)
                        container
                    } ?: container

                }
            }
        }

    }

    private fun launchActivity()
    {
        val intent = Intent(context,ActivityMusicPlayback::class.java).apply {
            putExtra("position", LIVE_POSITION_DATA.value)
            putParcelableArrayListExtra("musicList",musicList as ArrayList<out Parcelable>)
        }
        launcher.launch(intent)
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val container: RelativeLayout = itemView.findViewById(R.id.container)
        val btnPlayPause: ImageButton = itemView.findViewById(R.id.btn_play_pause)
        val coverImage: ShapeableImageView = itemView.findViewById(R.id.img_musicCover)
        val musicName: TextView = itemView.findViewById(R.id.txt_musicName)
        val musicArtist: TextView = itemView.findViewById(R.id.txt_musicArtist)


    }

}
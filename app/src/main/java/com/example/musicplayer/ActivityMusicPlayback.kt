package com.example.musicplayer

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.android.material.slider.Slider
import com.google.android.material.slider.Slider.OnChangeListener
import com.google.android.material.slider.Slider.OnSliderTouchListener
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ActivityMusicPlayback : AppCompatActivity() {

    val _liveCurrPostion = MutableLiveData<Int?>(null)
    private val currPosition: LiveData<Int?> = _liveCurrPostion
    private lateinit var musicList:ArrayList<Music>

    lateinit var btnPlayPause:ImageView
    lateinit var timeSlider:Slider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_music_playback)
        handleBackPressed()

        musicList = intent.getParcelableArrayListExtra<Music>("musicList")!!
        _liveCurrPostion.value = intent.getIntExtra("position",0)

        timeSlider = findViewById(R.id.slider_time)
        timeSlider.valueFrom = 0f
        timeSlider.addOnSliderTouchListener(getSliderTouchListener())

        val coverRecyclerView = findViewById<RecyclerView>(R.id.music_covers)
        val adapterMusicCover = AdapterMusicCover(musicList,this)

        coverRecyclerView.adapter = adapterMusicCover
        coverRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        coverRecyclerView.scrollToPosition(_liveCurrPostion.value!!)


        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(coverRecyclerView)

        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnPlayPause.isSelected = true
        btnPlayPause.setOnClickListener{ btn->
            if(!btn.isSelected)
            {
                btn.isSelected = true
                myMediaPlayer.pauseMusic()
            }
            else{
                btn.isSelected = false
                if(myMediaPlayer.live_Duration.value != myMediaPlayer.live_CurrentTime.value)
                    myMediaPlayer.resumeMusic()
                else
                    myMediaPlayer.replayMusic()
            }
        }
        btnPlayPause.isClickable = false


        val nextTrackBtn = findViewById<ImageView>(R.id.next_track)
        nextTrackBtn.setOnClickListener{
            _liveCurrPostion.value?.let {
                coverRecyclerView.smoothScrollToPosition(it + 1)
            }

        }

        val prevTrackBtn = findViewById<ImageView>(R.id.prev_track)
        prevTrackBtn.setOnClickListener{
            _liveCurrPostion.value?.let {
                coverRecyclerView.smoothScrollToPosition(it - 1)
            }
        }

        coverRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position: Int = getCurrentItem(recyclerView)
                    if(position!=-1 && position!=_liveCurrPostion.value) {
                        _liveCurrPostion.value = position

                        btnPlayPause.isSelected = myMediaPlayer.mediaPlayer.isPlaying != true
                        btnPlayPause.isClickable = false
                    }
                }
            }
        })


        trackCurrPosition()
        trackDurationLiveData()
        trackCurrentTimeLiveData()
    }


    private fun trackCurrPosition() {

        currPosition.observe(this){
            val musicName:TextView = findViewById(R.id.txt_musicName)
            val musicArtist:TextView = findViewById(R.id.txt_musicArtist)

            musicName.text = musicList[it!!].music_name
            musicArtist.text = musicList[it].artist

            changeBackGroundGradient()
            musicList[it].musicURL?.let {
                it1 -> myMediaPlayer.playNewMusic(it1)
            }

        }
    }

    private fun changeBackGroundGradient() {

        CoroutineScope(Dispatchers.IO).launch {
            val pl_view = findViewById<ConstraintLayout>(R.id.playback_view)
            val bitmap = Picasso.get().load(musicList[_liveCurrPostion.value!!].musicCoverURL).get()

            Palette.from(bitmap).generate {
                it?.getDarkVibrantColor(0x000000)?.let { color ->

                    val newAlpha = (color * (225 * 0.45).toInt())// 50% transparency
                    val newColor = (newAlpha shl 24) or (color and 0x00FFFFFF)

                    val colors: IntArray = intArrayOf(newColor, Color.parseColor("#000000"))

                    //create a new gradient color

                    //create a new gradient color
                    val gd = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM, colors)

                    gd.setCornerRadius(0f)
                    pl_view.background = gd

                }
            }
        }

    }

    private fun getCurrentItem(recyclerView: RecyclerView): Int {

        return (recyclerView.layoutManager as LinearLayoutManager)
            .findFirstCompletelyVisibleItemPosition()
    }

    private fun trackDurationLiveData()
    {
        val musicEndTime:TextView = findViewById(R.id.txt_totalTime)

        myMediaPlayer.live_Duration.observe(this){
            it?.let {
                musicEndTime.text = it
                btnPlayPause.isSelected = false
                btnPlayPause.isClickable = true
                Log.e("Tag","Running")
                if(it!="0:00")
                    timeSlider.valueTo = timeStringToMillis(it)
            }
        }
    }

    private fun timeStringToMillis(time: String): Float {
        val parts = time.split(":")

        val minutes = parts[0].toInt()
        val seconds = parts[1].toInt()

        return (minutes * 60 + seconds) * 1000f
    }

    private fun millisToTimeString(time:Int):String{

        val durationInSeconds = time / 1000
        val minutes = durationInSeconds / 60
        val seconds = durationInSeconds % 60
        return "$minutes:${if (seconds % 10 == seconds) "0$seconds" else "$seconds"}"

    }

    private fun trackCurrentTimeLiveData()
    {
        myMediaPlayer.live_CurrentTime.observe(this){ it1->
            val musicCurrentTime:TextView = findViewById(R.id.txt_currentTime)
            it1?.let {it2->
                musicCurrentTime.text = it2
                timeSlider.value = timeStringToMillis(it2)
                if(it2==myMediaPlayer.live_Duration.value)
                    btnPlayPause.isSelected = true
            }
        }
    }

    private fun getSliderTouchListener(): OnSliderTouchListener{

        val listener = getSliderOnChangeListener()
        return object :OnSliderTouchListener{
            override fun onStartTrackingTouch(p0: Slider) {
                myMediaPlayer.job?.cancel()
                p0.addOnChangeListener(getSliderOnChangeListener())
            }

            override fun onStopTrackingTouch(p0: Slider) {
                p0.removeOnChangeListener(listener)
                myMediaPlayer.setTime(p0.value.toInt())
                btnPlayPause.isSelected = false
            }
        }
    }

    private fun getSliderOnChangeListener(): OnChangeListener {

        return OnChangeListener { p0, time, p2 ->
            val musicCurrentTime:TextView = findViewById(R.id.txt_currentTime)
            musicCurrentTime.text = millisToTimeString(time.toInt())
        }
    }


    private fun handleBackPressed()
    {
        onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            val returnIntent = Intent()
            returnIntent.putExtra("position", _liveCurrPostion.value)
            setResult(RESULT_OK, returnIntent)
            myMediaPlayer.live_Duration.removeObservers(this@ActivityMusicPlayback)
            myMediaPlayer.live_CurrentTime.removeObservers(this@ActivityMusicPlayback)
            finish()
        }

    }

}
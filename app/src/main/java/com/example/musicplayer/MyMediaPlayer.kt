package com.example.musicplayer

import android.media.MediaPlayer
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object myMediaPlayer {

    val mediaPlayer: MediaPlayer = MediaPlayer()

    val live_Duration = MutableLiveData<String?>(null)
    val live_CurrentTime = MutableLiveData<String?>(null)

    private var length:Int? = null
    var job: Job? =null


    fun playNewMusic(url:String){

        length = null
        mediaPlayer.reset()
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()

        job?.cancel()
        live_CurrentTime.value = "0:00"
        live_Duration.value = "0:00"


        mediaPlayer.setOnPreparedListener {
            it.start()
            setDuration()
            setCurrentTime()
        }

    }

    fun pauseMusic(){
        mediaPlayer.pause()
        length = mediaPlayer.currentPosition
        job?.cancel()
    }

    fun resumeMusic(){
        length?.let {
            mediaPlayer.seekTo(it)
            mediaPlayer.start()
            setCurrentTime()
        }
    }

    fun replayMusic()
    {
        mediaPlayer.start()
        live_CurrentTime.value = "0:00"
        setCurrentTime()
    }

    fun setTime(time:Int)
    {
        length = time
        resumeMusic()

    }

    private fun setDuration()
    {
        val durationInSeconds = mediaPlayer.duration / 1000
        val minutes = durationInSeconds / 60
        val seconds = durationInSeconds % 60

        live_Duration.value =  "$minutes:$seconds"
    }

    private fun setCurrentTime()
    {
        job = CoroutineScope(Dispatchers.Default).launch {
            if(isActive) {
                while (mediaPlayer.isPlaying) {
                    val durationInSeconds = mediaPlayer.currentPosition / 1000
                    val minutes = durationInSeconds / 60
                    val seconds = durationInSeconds % 60
                    withContext(Dispatchers.Main) {
                        live_CurrentTime.value =
                            "$minutes:${if (seconds % 10 == seconds) "0$seconds" else "$seconds"}"
                    }
                }
            }
        }

    }


}

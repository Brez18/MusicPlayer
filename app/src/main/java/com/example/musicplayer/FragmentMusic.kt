package com.example.musicplayer

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mytravelapp.Server.Caller
import com.example.mytravelapp.Server.RetrofitHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class FragmentMusic : Fragment() {

    private val musicApi = RetrofitHelper.getInstance().create(Caller::class.java)
    private val musicList = mutableListOf<Music>()

    private val launcher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()) { result ->
        result?.let {
            if (it.resultCode == Activity.RESULT_OK) {
                val newPosition = it.data?.getIntExtra("position", 0)
                newPosition?.let {
                    AdapterMusicRecyclerView.LIVE_POSITION_DATA.value = newPosition
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment__music, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val musicRecyclerView = view.findViewById<RecyclerView>(R.id.recycler_musicList)


        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {

            loadMusicData()

            withContext(Dispatchers.Main) {
                val musicAdapter = context?.let { AdapterMusicRecyclerView(musicList, it, launcher) }
                musicRecyclerView.adapter = musicAdapter
                musicRecyclerView.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }
        }

    }

    private suspend fun loadMusicData()
    {
        val response = musicApi.getMusicData()

        if(response.code() == 200){

            val responseString = response.body()?.string()
            val jsonResponse = responseString?.let { JSONObject(it) }

            jsonResponse?.getJSONArray("data")?.let {
                val dataSize = it.length()

                for (num in 0 until dataSize){
                    val musicObj = it.getJSONObject(num)

                    val musicName = musicObj.getString("name")
                    val musicArtist = musicObj.getString("artist")
                    val coverID = musicObj.getString("cover")
                    var musicUrl = musicObj.getString("url")
                    val coverUrl = "https://cms.samespace.com/assets/$coverID"

                    musicUrl = filterUrl(musicUrl)
                    musicList.add(Music(musicName,musicArtist, coverUrl ,musicUrl))
                }
            }
        }
    }

    private fun filterUrl(musicUrl: String): String {

        var url = musicUrl.replace(" ","")

        if (!musicUrl.startsWith("https://"))
            url = musicUrl.replace(" ","").replace("https-","https://")

        if(!url.endsWith(".mp3"))
            url = url.replace("-mp3",".mp3")

        if(!url.contains("pub-172b4845a7e24a16956308706aaf24c2.r2.dev/"))
            url = url.replace("pub-172b4845a7e24a16956308706aaf24c2-r2-dev-","pub-172b4845a7e24a16956308706aaf24c2.r2.dev/")

        return  url
    }


}
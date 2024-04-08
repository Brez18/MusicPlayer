package com.example.musicplayer

import android.os.Bundle
import android.util.Log
import android.view.View.OnLayoutChangeListener
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

class ActivityHome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val viewPager2 = findViewById<ViewPager2>(R.id.homeViewPager)
        val viewPagerAdapter = AdapterViewPager(this)
        viewPager2.adapter = viewPagerAdapter

        val tabLayout = findViewById<TabLayout>(R.id.homeTabLayout)

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener{
            override fun onTabSelected(p0: TabLayout.Tab?) {
                val position = p0?.position
                position?.let {
                    if(viewPager2.currentItem!=position) {
                        viewPager2.setCurrentItem(position, true)
                    }
                }
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {}

            override fun onTabReselected(p0: TabLayout.Tab?) {}
        })

        viewPager2.registerOnPageChangeCallback(object: OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })


    }
}
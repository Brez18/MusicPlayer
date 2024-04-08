package com.example.musicplayer

import android.os.Parcel
import android.os.Parcelable

data class Music(
    val music_name: String?,
    val artist: String?,
    val musicCoverURL: String?,
    val musicURL: String?,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(music_name)
        parcel.writeString(artist)
        parcel.writeString(musicCoverURL)
        parcel.writeString(musicURL)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Music> {
        override fun createFromParcel(parcel: Parcel): Music {
            return Music(parcel)
        }

        override fun newArray(size: Int): Array<Music?> {
            return arrayOfNulls(size)
        }
    }
}

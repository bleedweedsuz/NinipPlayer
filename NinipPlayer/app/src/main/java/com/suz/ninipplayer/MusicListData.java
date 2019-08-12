package com.suz.ninipplayer;

import android.graphics.Bitmap;

public class MusicListData {
    public Integer ID;
    public String URL;
    public String Name;
    public Bitmap art;
    public String Size;
    //------------------------>
    public MusicListData(String URL,String Name,Bitmap art,String Size){
        this.URL = URL;
        this.Name = Name;
        this.Size=Size;
        this.art = art;
    }
    public MusicListData(Integer ID,String URL,String Name,Bitmap art,String Size){
        this.ID =ID;
        this.URL = URL;
        this.Name = Name;
        this.Size=Size;
        this.art = art;
    }

    @Override
    public String toString() {
        return "URL:" +URL + " NAME:" + Name;
    }
}

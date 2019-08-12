package com.suz.ninipplayer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
public class MusicUtils {
    public static Bitmap getAlbumart(String path){
        Bitmap albumArt = null;
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(path);
            byte rawData[] = mediaMetadataRetriever.getEmbeddedPicture();
            albumArt = BitmapFactory.decodeByteArray(rawData, 0, rawData.length);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return albumArt;
    }
}

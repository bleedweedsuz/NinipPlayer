package com.suz.ninipplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE TLIST (ID INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT NOT NULL,CREATEDATE TEXT NOT NULL,TYPE INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE SLIST (ID INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT NOT NULL,URL TEXT NOT NULL,SIZE STRING NOT NULL,TID INTEGER NOT NULL)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //--->
    }
}

package com.suz.ninipplayer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
public class TempList extends ActionBarActivity implements AdapterView.OnItemLongClickListener,AdapterView.OnItemClickListener {
    SQLiteDatabase database;
    ListView musicListView;
    int type;//0=>song_list,1=>play_list,2=stream_list
    public ArrayList<MusicListData> tempList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.templist);
        Initialize();
    }
    private void Initialize(){
        Bitmap backImage = BitmapFactory.decodeResource(getResources(),R.drawable.actionbar);
        getSupportActionBar().setBackgroundDrawable(new BitmapDrawable(getResources(), backImage));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.navigate_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        musicListView = (ListView) findViewById(R.id.musicListView);
        musicListView.setOnItemClickListener(this);
        musicListView.setOnItemLongClickListener(this);
        type= getIntent().getIntExtra("type",0);
        loadData();
    }
    private void loadData(){
        if(type==0){
            ListAllSong();
        }
        else if(type ==1){
            getSupportActionBar().setTitle("Playlist Collection");
            LoadPlaylist();
        }
        else if(type ==2){
            getSupportActionBar().setTitle("Stream Music Collection");
            LoadStreamList();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        else if(item.getItemId() == R.id.addNew){
            if(type==0){//song list
                //------->DO NOTHING ---....
            }
            else if(type ==1){//playlist
                AddNewPlayList();
            }
            else if(type ==2){//stream music
                AddNewStream();
            }
            return true;
        }
        else if(item.getItemId() == R.id.reLoad){
            loadData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_music, menu);
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.slide_right, R.anim.slide_right_out);
    }
    private void LoadPlaylist(){
        try{
            tempList.clear();
            DBHelper dbHelper = new DBHelper(this, MainActivity.DBNAME,null,MainActivity.VERSION);
            database = dbHelper.getWritableDatabase();
            Cursor cursor = database.rawQuery("SELECT * FROM TLIST WHERE TLIST.TYPE=1",null);
            for(int i=0;i<cursor.getCount();i++){
                cursor.moveToNext();
                tempList.add(new MusicListData(cursor.getInt(0) ,"",cursor.getString(1),null,"Create Date " +cursor.getString(2)));
            }
            musicListView.setAdapter(new MusicListAdapter(this));
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        finally {
            database.close();
        }
    }
    private void LoadStreamList(){
        try{
            tempList.clear();
            DBHelper dbHelper = new DBHelper(this, MainActivity.DBNAME,null,MainActivity.VERSION);
            database = dbHelper.getWritableDatabase();
            Cursor cursor = database.rawQuery("SELECT * FROM TLIST WHERE TLIST.TYPE=2",null);
            for(int i=0;i<cursor.getCount();i++){
                cursor.moveToNext();
                tempList.add(new MusicListData(cursor.getInt(0) ,"",cursor.getString(1),null,"Create Date " +cursor.getString(2)));
            }
            musicListView.setAdapter(new MusicListAdapter(this));
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        finally {
            database.close();
        }
    }
    public void ListAllSong(){
        try{
            tempList.clear();
            {
                File rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                if(rootDir.exists()) {
                    for (File file : rootDir.listFiles()) {
                        if (file.isFile() && (file.getName().endsWith(".mp3")) || file.getName().endsWith(".MP3")) {
                            AddFile(file);
                        }
                    }
                }
            }
            {
                File rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                if(rootDir.exists()) {
                    for (File file : rootDir.listFiles()) {
                        if (file.isFile() && (file.getName().endsWith(".mp3")) || file.getName().endsWith(".MP3")) {
                            AddFile(file);
                        }
                    }
                }
            }
            {
                File rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if(rootDir.exists()) {
                    for (File file : rootDir.listFiles()) {
                        if (file.isFile() && (file.getName().endsWith(".mp3")) || file.getName().endsWith(".MP3")) {
                            AddFile(file);
                        }
                    }
                }
            }
            {
                File rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                if(rootDir.exists()) {
                    for (File file : rootDir.listFiles()) {
                        if (file.isFile() && (file.getName().endsWith(".mp3")) || file.getName().endsWith(".MP3")) {
                            AddFile(file);
                        }
                    }
                }
            }
            {
                File rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
                if(rootDir.exists()) {
                    for (File file : rootDir.listFiles()) {
                        if (file.isFile() && (file.getName().endsWith(".mp3")) || file.getName().endsWith(".MP3")) {
                            AddFile(file);
                        }
                    }
                }
            }
            musicListView.setAdapter(new MusicListAdapter(this));
        }
        catch(Exception ex){
            Log.e("Error_Tags", ex.toString());
        }
    }
    private void AddFile(File file) {
        String tempUrl = file.getAbsolutePath() +"/";
        String tempName = file.getName().substring(0, file.getName().length() - 4).toString();
        String tempSize = String.valueOf(file.length()/1000000);//Constant size to convert BYTE TO MB
        tempList.add(new MusicListData(tempUrl, tempName, MusicUtils.getAlbumart(file.getAbsolutePath().toString()), "Size: " + tempSize + " mb"));
    }
    private void AddNewPlayList(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = getLayoutInflater().inflate(R.layout.newplaylistbox, null, true);
        builder.setView(view);
        builder.setTitle("Add New Playlist");
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String CDate = sdf.format(Calendar.getInstance().getTime());
                EditText pName = (EditText)view.findViewById(R.id.pBox);
                DBHelper dbHelper = new DBHelper(TempList.this, MainActivity.DBNAME, null, MainActivity.VERSION);
                database = dbHelper.getWritableDatabase();
                database.execSQL("INSERT INTO TLIST VALUES (NULL,'" + pName.getText().toString() + "','" + CDate + "',1)");
                dialog.cancel();
                LoadPlaylist();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    private void AddNewStream(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = getLayoutInflater().inflate(R.layout.newplaylistbox,null,true);
        builder.setView(view);
        builder.setTitle("Add Stream List Name");
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String CDate = sdf.format(Calendar.getInstance().getTime());
                EditText pName = (EditText)view.findViewById(R.id.pBox);
                DBHelper dbHelper = new DBHelper(TempList.this, MainActivity.DBNAME, null, MainActivity.VERSION);
                database = dbHelper.getWritableDatabase();
                database.execSQL("INSERT INTO TLIST VALUES (NULL,'" + pName.getText().toString() + "','" + CDate + "',2)");
                dialog.cancel();
                LoadStreamList();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(type==0){
            //add All Song
            MainActivity.RMenuID =0;
        }
        else if(type==1){
            //add playlist song
            ArrayList<MusicListData> tList = new ArrayList<>();
            try{
                DBHelper dbHelper = new DBHelper(this, MainActivity.DBNAME,null,MainActivity.VERSION);
                database = dbHelper.getWritableDatabase();
                Cursor cursor = database.rawQuery("SELECT * FROM SLIST WHERE TID=?" ,new String[]{tempList.get(position).ID.toString()});
                for(int i=0;i<cursor.getCount();i++){
                    cursor.moveToNext();
                    File file = new File(cursor.getString(2));
                    String tempUrl = file.getAbsolutePath() +"/";
                    String tempName = file.getName().substring(0, file.getName().length() - 4).toString();
                    String tempSize = String.valueOf(file.length()/1000000);//Constant size to convert BYTE TO MB
                    MusicListData musicListData = new MusicListData(cursor.getInt(0),tempUrl, tempName, MusicUtils.getAlbumart(file.getAbsolutePath().toString()), "Size: " + tempSize + " mb");
                    tList.add(musicListData);
                }
                cursor.close();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
            finally {
                database.close();
            }
            MainActivity.tempList = tList;
            MainActivity.RMenuID =2;
            MainActivity.newDataArrive =true;
        }
        else if(type==2){
            //add stream song
            ArrayList<MusicListData> tList = new ArrayList<>();
            try{
                DBHelper dbHelper = new DBHelper(this, MainActivity.DBNAME,null,MainActivity.VERSION);
                database = dbHelper.getWritableDatabase();
                Cursor cursor = database.rawQuery("SELECT * FROM SLIST WHERE TID=?" ,new String[]{tempList.get(position).ID.toString()});
                for(int i=0;i<cursor.getCount();i++){
                    cursor.moveToNext();
                    File file = new File(cursor.getString(2));
                    String tempUrl = file.getAbsolutePath() +"/";
                    String tempName = file.getName().substring(0, file.getName().length() - 4).toString();
                    String tempSize = String.valueOf(file.length()/1000000);//Constant size to convert BYTE TO MB
                    MusicListData musicListData = new MusicListData(cursor.getInt(0),tempUrl, tempName, MusicUtils.getAlbumart(file.getAbsolutePath().toString()), "Size: " + tempSize + " mb");
                    tList.add(musicListData);
                }
                cursor.close();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
            finally {
                database.close();
            }
            MainActivity.tempList = tList;
            MainActivity.RMenuID =1;
            MainActivity.newDataArrive =true;
        }
        onBackPressed();
    }
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        if(type ==0){
            final int FinalPosition = position;
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            ListView listView = new ListView(this);
            listView.setPadding(5,5,5,5);
            listView.setDividerHeight(1);
            ArrayList<String> pList = new ArrayList<>();
            final ArrayList<Integer> pIDList = new ArrayList<>();
            try{
                DBHelper dbHelper = new DBHelper(this, MainActivity.DBNAME,null,MainActivity.VERSION);
                database = dbHelper.getWritableDatabase();
                Cursor cursor = database.rawQuery("SELECT * FROM TLIST WHERE TLIST.TYPE=1",null);
                for(int i=0;i<cursor.getCount();i++){
                    cursor.moveToNext();
                    pList.add(new String(cursor.getString(1)));
                    pIDList.add(new Integer(cursor.getInt(0)));
                }
                cursor.close();
                listView.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,pList));
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
            finally {
                database.close();
            }
            builder.setTitle("Choose PlayList To Add");
            builder.setView(listView);
            final AlertDialog dialog = builder.show();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                    try {
                        DBHelper dbHelper = new DBHelper(TempList.this, MainActivity.DBNAME, null, MainActivity.VERSION);
                        database = dbHelper.getWritableDatabase();
                        String qry="INSERT INTO SLIST VALUES (NULL,'" + tempList.get(FinalPosition).Name + "','" + tempList.get(FinalPosition).URL + "','" + tempList.get(FinalPosition).Size + "','" + pIDList.get(pos).toString() + "')";
                        database.execSQL(qry);
                        Toast.makeText(TempList.this,"Song Added To PlayList",Toast.LENGTH_SHORT).show();
                        Log.d("-->",qry);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        database.close();
                    }
                    dialog.dismiss();
                }
            });
        }
        else if(type==1){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose Options Below");
            builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    ShowEADialog(2,position);
                }
            });
            builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        DBHelper dbHelper = new DBHelper(TempList.this, MainActivity.DBNAME, null, MainActivity.VERSION);
                        database = dbHelper.getWritableDatabase();
                        database.execSQL("DELETE FROM TLIST WHERE ID='" + tempList.get(position).ID + "'");
                        if (type == 1) {
                            LoadPlaylist();
                        } else if (type == 2) {
                            LoadStreamList();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        database.close();
                    }
                }
            });
            builder.show();
        }
        else if (type == 2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose Options Below");
            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    ShowEADialog(1,position);
                }
            });
            builder.setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    ShowEADialog(2,position);
                }
            });
            builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        DBHelper dbHelper = new DBHelper(TempList.this, MainActivity.DBNAME, null, MainActivity.VERSION);
                        database = dbHelper.getWritableDatabase();
                        database.execSQL("DELETE FROM TLIST WHERE ID='" + tempList.get(position).ID + "'");
                        if (type == 1) {
                            LoadPlaylist();
                        } else if (type == 2) {
                            LoadStreamList();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        database.close();
                    }
                }
            });
            builder.show();
        }
        return true;
    }
    private void ShowEADialog(int mode,final int position){
        if(mode ==1){
            //ADD
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final View view = getLayoutInflater().inflate(R.layout.newplaylistbox,null,true);
            builder.setView(view);
            builder.setTitle("Add New");
            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        EditText pName = (EditText)view.findViewById(R.id.pBox);
                        DBHelper dbHelper = new DBHelper(TempList.this, MainActivity.DBNAME, null, MainActivity.VERSION);
                        database = dbHelper.getWritableDatabase();

                        ContentValues cv= new ContentValues();
                        cv.put("NAME",pName.getText().toString());
                        cv.put("URL",pName.getText().toString());
                        cv.put("SIZE","STREAM");
                        cv.put("TID",tempList.get(position).ID);
                        database.insert("SLIST",null,cv);
                        Toast.makeText(TempList.this,"New URL Added",Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    finally {
                        database.close();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
        else if(mode ==2){
            //EDIT
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final View view = getLayoutInflater().inflate(R.layout.newplaylistbox,null,true);
            final EditText editText =(EditText)view.findViewById(R.id.pBox);
            editText.setText(tempList.get(position).Name);

            builder.setView(view);
            builder.setTitle("Edit");
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        DBHelper dbHelper = new DBHelper(TempList.this, MainActivity.DBNAME, null, MainActivity.VERSION);
                        database = dbHelper.getWritableDatabase();
                        database.execSQL("UPDATE TLIST SET NAME='" + editText.getText().toString() + "' WHERE ID='" + tempList.get(position).ID + "'");
                        LoadStreamList();
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    finally {
                        database.close();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
    }
    //ArrayAdapter Music List
    private class MusicListAdapter extends ArrayAdapter<MusicListData> {
        Activity activity;
        public MusicListAdapter(Activity activity){
            super(activity,R.layout.music_list_layout,tempList);
            this.activity = activity;
        }
        @Override
        public int getCount() {
            return tempList.size();
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = activity.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.music_list_layout, null, true);
            MusicListData mlData = tempList.get(position);
            ImageView icon = (ImageView) rowView.findViewById(R.id.music_icon);
            Bitmap bmp = tempList.get(position).art;
            if(bmp== null){
                icon.setImageResource(R.drawable.music_player);
            }
            else{
                icon.setImageBitmap(bmp);
            }
            TextView title = (TextView) rowView.findViewById(R.id.music_Title);
            title.setText(mlData.Name);
            TextView size = (TextView) rowView.findViewById(R.id.music_size);
            size.setText(mlData.Size);
            return rowView;
        }
    }
}

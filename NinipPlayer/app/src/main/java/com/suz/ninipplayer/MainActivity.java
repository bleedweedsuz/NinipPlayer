package com.suz.ninipplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends ActionBarActivity implements View.OnClickListener,AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener{
    SQLiteDatabase database;
    public static boolean newDataArrive= false;
    boolean isPlay =false;
    public static String DBNAME ="NPLAYER";
    public static int VERSION =1;
    public static int RMenuID =0;//0=>All song,1=>Stream Music,2=>Playlist
    public MediaPlayer mediaPlayer;
    private ListView navigationList;
    private ListView musicListView;
    private DrawerLayout dLayout;
    private RelativeLayout mainScreenLayout;
    ImageView imageView;
    ImageView ppBtn,nextBtn,prvBtn;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    public static ArrayList<MusicListData> tempList = new ArrayList<>();
    public int index =0;
    boolean isDragging = false;
    SeekBar seekBar;
    Thread seekBarThread;
    TextView cView,eView;
    TextView titleName,subTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaPlayer = new MediaPlayer();
       Initialize();
        InitializeNavigation();
        LoadList();
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        cView =(TextView)findViewById(R.id.currTime);
        eView =(TextView)findViewById(R.id.endTime);
        titleName =(TextView)findViewById(R.id.titleName);
        subTitle =(TextView)findViewById(R.id.subTitle);
    }
    @Override
    protected void onResume() {
        super.onResume();
        newDataArriveListener();
    }
    private void newDataArriveListener(){
        try {
            if(RMenuID == 0 || RMenuID ==1) {
                LoadList();
                index = 0;
                if (newDataArrive) {
                    if (tempList.size() > 0) {
                        if (mediaPlayer == null) {
                            mediaPlayer = MediaPlayer.create(this, Uri.parse(tempList.get(index).URL));
                        } else {
                            isPlay = false;
                            mediaPlayer.stop();
                        }
                        mediaPlayer = MediaPlayer.create(this, Uri.parse(tempList.get(index).URL));
                        mediaPlayer.start();
                        ppBtn.setImageResource(R.drawable.pause);
                        isPlay = true;
                        PlayModeOn();
                    }
                    newDataArrive = false;
                }
            }
            else{
                Log.d("--->","STREAM 1");
                LoadList();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    private void LoadList(){
        if(RMenuID ==0){
            ListAllSong();
        }
        else if(RMenuID ==1){
            musicListView.setAdapter(new MusicListAdapter(this));
        }
        else if(RMenuID ==2){
            musicListView.setAdapter(new MusicListAdapter(this));
        }
    }
    private void Initialize(){
        try{
            mainScreenLayout = (RelativeLayout) findViewById(R.id.main_player_States);
            musicListView = (ListView)findViewById(R.id.dataContainList);
            musicListView.setOnItemLongClickListener(this);
            InitializeImageViewButtons();
            CreateMediaPlayer();
        }
        catch(Exception ex){
            Log.e("Error",ex.toString());
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            if(!dLayout.isDrawerOpen(findViewById(R.id.Drawer_Content))){
                dLayout.openDrawer(findViewById(R.id.Drawer_Content));
                dLayout.closeDrawer(findViewById(R.id.dataListDrawerLayout));
                getSupportActionBar().setTitle("Menu");
            }
            else {
                dLayout.closeDrawer(findViewById(R.id.Drawer_Content));
            }
        }
        else if(item.getItemId() == R.id.show_list){
            if(!dLayout.isDrawerOpen(findViewById(R.id.dataListDrawerLayout))){
                dLayout.openDrawer(findViewById(R.id.dataListDrawerLayout));
                dLayout.closeDrawer(findViewById(R.id.Drawer_Content));
                switch (RMenuID) {
                    case 0:{getSupportActionBar().setTitle("All Song");}break;
                    case 1:{getSupportActionBar().setTitle("Stream Music");}break;
                    case 2:{getSupportActionBar().setTitle("Playlist");}break;
                }
            }
            else {
                dLayout.closeDrawer(findViewById(R.id.dataListDrawerLayout));
            }
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    private void InitializeImageViewButtons(){
        ppBtn =(ImageView) findViewById(R.id.ppBtn);ppBtn.setOnClickListener(this);
        nextBtn =(ImageView) findViewById(R.id.nextBtn);nextBtn.setOnClickListener(this);
        prvBtn=(ImageView) findViewById(R.id.prvBtn);prvBtn.setOnClickListener(this);
    }
    private void InitializeNavigation(){
        //Install Navigation List
        navigationList =(ListView) findViewById(R.id.navigationList);
        NavigationListInstaller navigationListInstaller = new NavigationListInstaller(this);
        navigationList.setAdapter(navigationListInstaller.navigationArrayAdapter);
        navigationList.setOnItemClickListener(this);
        musicListView.setOnItemClickListener(this);
        dLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        dLayout.setScrimColor(Color.argb(80, 0, 187, 168));
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,dLayout,R.string.app_name,R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle(getString(R.string.app_name));
            }
        };
        //----------------------------------------------------->
        Bitmap backImage = BitmapFactory.decodeResource(getResources(), R.drawable.actionbar);
        getSupportActionBar().setBackgroundDrawable(new BitmapDrawable(getResources(), backImage));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
        actionBarDrawerToggle.setHomeAsUpIndicator(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.menu)));
        getSupportActionBar().setHomeButtonEnabled(true);
        dLayout.setDrawerListener(actionBarDrawerToggle);
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.navigationList) {
            //Select Item
            switch (position) {
                case 0: {
                    //MediaPlayer
                    OpenMediaPlayerAllSong();
                }
                break;
                case 1: {
                    //Stream Music
                    OpenStreamMusicPlayer();
                }
                break;
                case 2: {
                    //Playlist
                    OpenPlayListPlayer();
                }
                break;
                default:
                    break;
            }
        }
        else if(parent.getId() == R.id.dataContainList) {
            try {
                if(RMenuID ==0 || RMenuID ==1) {
                    for (int j = 0; j < parent.getChildCount(); j++) {
                        parent.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);
                        ((ImageView) parent.getChildAt(j).findViewById(R.id.music_icon)).setImageResource(R.drawable.music_player);
                    }
                    view.setBackgroundColor(Color.LTGRAY);
                    ((ImageView) view.findViewById(R.id.music_icon)).setImageResource(R.drawable.play);
                    isPlay = false;
                    if (mediaPlayer == null) {
                        index = position;
                        mediaPlayer = MediaPlayer.create(this, Uri.parse(tempList.get(index).URL));
                        mediaPlayer.start();
                        ppBtn.setImageResource(R.drawable.pause);
                        isPlay = true;
                        PlayModeOn();
                    } else {
                        if (index == position) {
                            if (mediaPlayer.isPlaying()) {
                                isPlay = false;
                                mediaPlayer.pause();
                                ppBtn.setImageResource(R.drawable.play);
                            } else {
                                mediaPlayer.stop();
                                mediaPlayer = MediaPlayer.create(this, Uri.parse(tempList.get(index).URL));
                                mediaPlayer.start();
                                ppBtn.setImageResource(R.drawable.pause);
                                isPlay = true;
                                PlayModeOn();
                            }
                        } else {
                            index = position;
                            mediaPlayer.stop();
                            mediaPlayer = MediaPlayer.create(this, Uri.parse(tempList.get(index).URL));
                            mediaPlayer.start();
                            ppBtn.setImageResource(R.drawable.pause);
                            isPlay = true;
                            PlayModeOn();
                        }
                    }
                }
                else{
                    if(mediaPlayer !=null){
                        mediaPlayer.stop();
                        //mediaPlayer.release();
                    }
                    String URL =  "http://" + tempList.get(index).URL.substring(5,tempList.get(index).URL.length() -1);
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(this, Uri.parse(URL));
                    Toast.makeText(this,"Buffering...",Toast.LENGTH_SHORT).show();
                    titleName.setText(tempList.get(index).Name);
                    subTitle.setText(tempList.get(index).URL);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                        @Override
                        public void onBufferingUpdate(MediaPlayer mp, int percent) {
                            subTitle.setText("buffering " + percent + " %");
                        }
                    });
                }
            }
            catch (Exception ex){
                Toast.makeText(this,ex.toString(),Toast.LENGTH_LONG).show();
            }
        }
        dLayout.closeDrawer(findViewById(R.id.dataListDrawerLayout));
        dLayout.closeDrawer(findViewById(R.id.Drawer_Content));
    }
    public void ReloadList(View view){
        ListAllSong();
    }
    private void CreateMediaPlayer(){
        try {
            imageView = new ImageView(this);
            //Modify the imageview
            imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setPadding(10, 10, 10, 10);
            imageView.setImageResource(R.drawable.album_art);
            //Add the album Art inside the mainScreenLayout
            mainScreenLayout.removeAllViews();
            mainScreenLayout.addView(imageView);
        }
        catch(Exception ex){
            Log.e("Error",ex.toString());
        }
    }
    private void OpenMediaPlayerAllSong(){
        Intent i=new Intent(this,TempList.class);
        i.putExtra("type",0);
        startActivity(i);
        this.overridePendingTransition(R.anim.slide_left, R.anim.slide_left_out);
    }
    private void OpenPlayListPlayer() {
        Intent i=new Intent(this,TempList.class);
        i.putExtra("type",1);
        startActivity(i);
        this.overridePendingTransition(R.anim.slide_left, R.anim.slide_left_out);
    }
    private void OpenStreamMusicPlayer() {
        Intent i=new Intent(this,TempList.class);
        i.putExtra("type",2);
        startActivity(i);
        this.overridePendingTransition(R.anim.slide_left, R.anim.slide_left_out);
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
            Log.e("Error_Tags",ex.toString());
        }
    }
    private void AddFile(File file) {
        String tempUrl = file.getAbsolutePath() +"/";
        String tempName = file.getName().substring(0,file.getName().length() - 4).toString();
        String tempSize = String.valueOf(file.length()/1000000);//Constant size to convert BYTE TO MB
        tempList.add(new MusicListData(tempUrl,tempName, MusicUtils.getAlbumart(file.getAbsolutePath().toString()),tempSize));
    }
    @Override
    public void onClick(View v) {
        if(RMenuID ==0 || RMenuID ==1){
            NotStream(v);
        }
        else{
            StreamMusic(v);
        }
    }
    public void NotStream(View v){
        try {
            switch (v.getId()) {
                case R.id.ppBtn: {
                    if (tempList.size() > 0) {
                        if (mediaPlayer == null) {
                            mediaPlayer = MediaPlayer.create(this, Uri.parse(tempList.get(index).URL));
                        }
                        if (mediaPlayer.isPlaying()) {
                            isPlay =false;
                            mediaPlayer.pause();
                            ppBtn.setImageResource(R.drawable.play);
                        } else {
                            mediaPlayer.start();
                            ppBtn.setImageResource(R.drawable.pause);
                            isPlay  = true;
                            PlayModeOn();
                        }
                    }
                }
                break;
                case R.id.nextBtn: {
                    if (tempList.size() > 0) {
                        if (mediaPlayer == null) {
                            mediaPlayer = MediaPlayer.create(this, Uri.parse(tempList.get(index).URL));
                        } else {
                            isPlay =false;
                            mediaPlayer.stop();
                        }
                        index++;
                        if (index >= tempList.size()) {
                            index = 0;
                        }
                        if (mediaPlayer != null) {
                            mediaPlayer = MediaPlayer.create(this, Uri.parse(tempList.get(index).URL));
                        }
                        if (mediaPlayer.isPlaying()) {
                            isPlay =false;
                            mediaPlayer.pause();
                            ppBtn.setImageResource(R.drawable.play);
                        } else {
                            mediaPlayer.start();
                            ppBtn.setImageResource(R.drawable.pause);
                            isPlay  = true;
                            PlayModeOn();
                        }
                    }
                }
                break;
                case R.id.prvBtn: {
                    if (tempList.size() > 0) {
                        if (mediaPlayer == null) {
                            mediaPlayer = MediaPlayer.create(this, Uri.parse(tempList.get(index).URL));
                        } else {
                            isPlay =false;
                            mediaPlayer.stop();
                        }
                        index--;
                        if (index < 0) {
                            index = tempList.size() - 1;
                        }
                        if (mediaPlayer != null) {
                            mediaPlayer = MediaPlayer.create(this, Uri.parse(tempList.get(index).URL));
                        }
                        if (mediaPlayer.isPlaying()) {
                            isPlay =false;
                            mediaPlayer.pause();
                            ppBtn.setImageResource(R.drawable.play);
                        } else {
                            mediaPlayer.start();
                            ppBtn.setImageResource(R.drawable.pause);
                            isPlay  = true;
                            PlayModeOn();
                        }
                    }
                }
                break;
                default:
                    break;
            }
        }
        catch (Exception ex){
            Toast.makeText(this,ex.toString(),Toast.LENGTH_LONG).show();
        }
    }
    public void StreamMusic(View v){
        try {
            switch (v.getId()) {
                case R.id.ppBtn: {
                    mediaPlayer.stop();
                    //mediaPlayer.release();
                }
                break;
                case R.id.nextBtn: {
                    if(mediaPlayer !=null){
                        mediaPlayer.stop();
                        //mediaPlayer.release();
                        index++;
                        if(index >= tempList.size()){
                            index=0;
                        }
                    }
                    String URL =  "http://" + tempList.get(index).URL.substring(5,tempList.get(index).URL.length() -1);
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(this, Uri.parse(URL));
                    Toast.makeText(this,"Buffering...",Toast.LENGTH_SHORT).show();
                    titleName.setText(tempList.get(index).Name);
                    subTitle.setText(tempList.get(index).URL);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                        @Override
                        public void onBufferingUpdate(MediaPlayer mp, int percent) {
                            subTitle.setText("buffering " + percent + " %");
                        }
                    });
                }
                break;
                case R.id.prvBtn: {
                    if(mediaPlayer !=null){
                        mediaPlayer.stop();
                        //mediaPlayer.release();
                        index++;
                        if(index < 0){
                            index= tempList.size() -1;
                        }
                    }
                    String URL =  "http://" + tempList.get(index).URL.substring(5,tempList.get(index).URL.length() -1);
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(this, Uri.parse(URL));
                    Toast.makeText(this,"Buffering...",Toast.LENGTH_SHORT).show();
                    titleName.setText(tempList.get(index).Name);
                    subTitle.setText(tempList.get(index).URL);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                        @Override
                        public void onBufferingUpdate(MediaPlayer mp, int percent) {
                            subTitle.setText("buffering " + percent + " %");
                        }
                    });
                }
                break;
                default:
                    break;
            }
        }
        catch (Exception ex){
            Toast.makeText(this,ex.toString(),Toast.LENGTH_LONG).show();
        }
    }
    public void PlayModeOn(){
        seekBar.setMax(mediaPlayer.getDuration());
        eView.setText(getTimeFromMills(mediaPlayer.getDuration()));
        titleName.setText(tempList.get(index).Name);
        subTitle.setText(tempList.get(index).URL);
        seekBarThread= new Thread(new Runnable() {
            @Override
            public void run() {
                while (isPlay) {
                    if(!isDragging) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mediaPlayer != null) {
                                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                    cView.setText(getTimeFromMills(mediaPlayer.getCurrentPosition()));
                                }
                            }
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //seekBar.setProgress(0);
                return;
            }
        });
        seekBarThread.start();
    }
    String getTimeFromMills(int millis){
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;
        String time = String.format("%02d:%02d:%02d", hour, minute, second);
        return time;
    }
    //ArrayAdapter List
    private class NavigationListInstaller{
        private ArrayList<NavigationData> tempList = new ArrayList<NavigationData>();
        public NavigationArrayAdapter navigationArrayAdapter;
        public NavigationListInstaller(Activity activity){
            //----------------Add All the Data here
            tempList.clear();
            {tempList.add(new NavigationData("Media Player",R.drawable.music));}
            //{tempList.add(new NavigationData("Video Player",R.drawable.video_player));}
            {tempList.add(new NavigationData("Stream Music",R.drawable.streammusic));}
            //{tempList.add(new NavigationData("Stream Video",R.drawable.video_player_stream));}
            {tempList.add(new NavigationData("Playlist",R.drawable.playlist));}
            //----------------Add NavigationArrayAdapter
            navigationArrayAdapter = new NavigationArrayAdapter(activity);
        }
        private class NavigationData{
            public String name;
            public int icon;
            public NavigationData(String name,int icon){
                this.name=name;
                this.icon=icon;
            }
        }
        private class NavigationArrayAdapter extends ArrayAdapter<NavigationData>{
            Activity activity;
            public NavigationArrayAdapter(Activity activity){
                super(activity,R.layout.navigation_layout,tempList);
                this.activity =activity;
            }
            @Override
            public int getCount() {
                return tempList.size();
            }
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = activity.getLayoutInflater();
                View rowView = inflater.inflate(R.layout.navigation_layout, null, true);
                NavigationData nData = tempList.get(position);
                //----------------------------------------------------->
                ImageView icon = (ImageView) rowView.findViewById(R.id.navigationIcon);
                icon.setImageResource(nData.icon);
                TextView name = (TextView) rowView.findViewById(R.id.navigationName);
                name.setText(nData.name);
                return rowView;
            }
        }
    }
    //ArrayAdapter Music List
    private class MusicListAdapter extends ArrayAdapter<MusicListData>{
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
            size.setText("Size:" +mlData.Size + " MB");
            return rowView;
        }
    }
    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isDragging =true;
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(RMenuID == 0 || RMenuID ==1) {
                isDragging = false;
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        }
    };

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        if(RMenuID ==1 || RMenuID ==2){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add you sure want to delete this");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                       if(index == position){
                           MainActivity.this.onClick(MainActivity.this.findViewById(R.id.ppBtn));
                        }
                        DBHelper dbHelper = new DBHelper(MainActivity.this, MainActivity.DBNAME, null, MainActivity.VERSION);
                        database = dbHelper.getWritableDatabase();
                        database.execSQL("DELETE FROM SLIST WHERE ID='" + tempList.get(position).ID + "'");
                        tempList.remove(position);
                        LoadList();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
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
        return false;
    }
}
package example.com.audioget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class MusicListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private ListView mListView;
    private Handler mHandler = new Handler();
    private ArrayList<MusicBean> mMediaLists = new ArrayList<MusicBean>();
    private MusicListAdapter adapter;
    private SimpleDateFormat sDateFormat    =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //DecimalFormat   fnum   =   new DecimalFormat("##0.00");
    //private SimpleDateFormat format = new SimpleDateFormat("mm:ss");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_list_layout);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        params.width = dm.widthPixels;
        params.height = dm.heightPixels/2 ;
        params.y = 0;
        params.x = 0;
        //params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;
        window.setAttributes(params);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        mListView = (ListView)findViewById(R.id.music_list_view_sec);
        mListView.setOnItemClickListener(this);
        adapter = new MusicListAdapter(this);
        mListView.setAdapter(adapter);
        asyncQueryMedia();
    }
//    public boolean onOptionsItemSelected(MenuItem item) {`
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                //MusicListAdapter adapter = (MusicListAdapter) parent.getAdapter();
//                //adapter.setPlayingPosition(position);
//                Intent intent = new Intent();
//                intent.putExtra("isTouch",12);
//                intent.putParcelableArrayListExtra("MUSIC_LIST", mMediaLists);
//                intent.putExtra("CURRENT_POSITION", 0);
//                setResult(1, intent);
//                return true;
//            default:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }
// 捕获返回键的方法2
@Override
public void onBackPressed() {
    Intent intent = new Intent();
    intent.putExtra("isTouch",12);
    setResult(1, intent);
    finish();
    super.onBackPressed();
}
    public void asyncQueryMedia() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mMediaLists.clear();

                queryMusic(Environment.getExternalStorageDirectory() + File.separator);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Log.e("MusicListActivity",mMediaLists.size()+"");
                        adapter.setListData(mMediaLists);
                    }
                });
            }
        }).start();
    }
    /**
     * 获取目录下的歌曲
     *
     * @param dirName
     */
    public void queryMusic(String dirName) {
        //歌曲文件的全路径 ：MediaStore.Audio.Media.DATA
        //Cursor cursor = contentResolver.query(Media.EXTERNAL_CONTENT_URI, projection, where, null, sortOrder);
        //String[] projection = {Media._ID,Media.DATA,Media.ORIENTATION};
       // String where =  "mime_type in ('audio/mpeg','audio/x-ms-wma') and bucket_display_name <> 'audio' and is_music > 0 " ;
       //String sortOrder = MediaStore.Audio.Media.DATA;
//        String[] projection = {
//                MediaStore.Audio.Media._ID,
//                MediaStore.Audio.Media.DISPLAY_NAME,
//                MediaStore.Audio.Media.DATA,
//                MediaStore.Audio.Media.ALBUM,
//                MediaStore.Audio.Media.ARTIST,
//                MediaStore.Audio.Media.DURATION,
//                MediaStore.Audio.Media.SIZE
//        };
//        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA,
//                new String[]{dirName + "%"},MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        //Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, where, null, sortOrder);
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null)
        {
            return;
        }
        // id title singer data time image
        MusicBean music;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
           //Log.e("MusicListActivity",""+cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
            //歌曲的路径cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
            if(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)).toLowerCase().indexOf("js")>0)
            {
                //如果不是音乐

                String isMusic = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC));
                if (isMusic != null && isMusic.equals("")) continue;
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                if (isRepeat(title, artist)) continue;
                music = new MusicBean();
                music.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
                music.setTitle(title);
                long updateTime= cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))*1000;
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                music.setArtist(artist);
                music.setUpdateTime(String.format("%.1f",size/1024.0)+"KB"+" | "+getTimeInfo(updateTime)+" ");
                music.setMusicPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                music.setLength(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                music.setImage(getAlbumImage(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))));
                mMediaLists.add(music);
            }
        }
        Collections.reverse(mMediaLists);
        cursor.close();
    }
    /**
     * 获取时间（年月日）
     * @return
     */
    public String getTimeInfo(long time){
        return sDateFormat.format(new Date(time));
    }

    /**
     * 根据音乐名称和艺术家来判断是否重复包含了
     * @param title
     * @param artist
     * @return
     */
    private boolean isRepeat(String title, String artist) {
        for (MusicBean music : mMediaLists) {
            if (title.equals(music.getTitle()) && artist.equals(music.getArtist())) {
                return true;
            }
        }
        return false;
    }
    /**
     * 根据歌曲id获取图片
     * @param albumId
     * @return
     */
    private String getAlbumImage(int albumId) {
        String result = "";
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(Uri.parse("content://media/external/audio/albums/" + albumId), new String[]{"album_art"}, null, null, null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); ) {
                result = cursor.getString(0);
                break;
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return null == result ? null : result;
    }
    /**
     *listView子项点击事件
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MusicListAdapter adapter = (MusicListAdapter) parent.getAdapter();
        adapter.setPlayingPosition(position);
        Intent intent = new Intent();
        intent.putExtra("isTouch",11);
        intent.putParcelableArrayListExtra("MUSIC_LIST", mMediaLists);
        intent.putExtra("CURRENT_POSITION", position);
        setResult(1, intent);
        finish();
    }
    //自定义适配器
    private class MusicListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private ArrayList<MusicBean> list = new ArrayList<MusicBean>();
        private int mPlayingPosition;
        public MusicListAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }
        public void setPlayingPosition(int position) {
            mPlayingPosition = position;
            notifyDataSetChanged();
        }
        public void setListData(ArrayList<MusicBean> list) {
            this.list = list;
            notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return list.size();
        }
        @Override
        public Object getItem(int position) {
            return list.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder= null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.music_list_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.icon = (ImageView)convertView.findViewById(R.id.music_list_icon);
                viewHolder.title = (TextView)convertView.findViewById(R.id.tv_music_list_title);
                viewHolder.artist = (TextView)convertView.findViewById(R.id.tv_music_list_artist);
                viewHolder.mark = convertView.findViewById(R.id.music_list_selected);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            if (mPlayingPosition == position)
                viewHolder.mark.setVisibility(View.VISIBLE);
            else
                viewHolder.mark.setVisibility(View.INVISIBLE);
            MusicBean music = (MusicBean) getItem(position);
            Bitmap icon = BitmapFactory.decodeFile(music.getImage());
            viewHolder.icon.setImageBitmap(icon == null ? BitmapFactory.decodeResource(getResources(), R.drawable.music) : icon);
            //viewHolder.title.setText(music.getArtist()+" - "+music.getTitle());
            viewHolder.title.setText(music.getTitle());
            viewHolder.artist.setText(music.getUpdateTime());
            return convertView;
        }
        class ViewHolder {
            ImageView icon;
            TextView title, artist;
            View mark;
        }
    }
}
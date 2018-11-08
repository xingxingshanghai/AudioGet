package example.com.audioget;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;
import java.util.ArrayList;

public class MusicServiceDemo extends Service {
    private MediaPlayer mPlayer;
    private ArrayList<MusicBean> musicPathLists;
    private int currentPos;
    Notification notification;
    Notification.Builder builder;
    String ns = Context.NOTIFICATION_SERVICE;
    NotificationManager mNotificationManager;
    public interface CallBack {
        boolean isPlayerMusic();
        int callTotalDate();
        int callCurrentTime();
        void iSeekTo(int m_second);
        void isPlayPre();
        void isPlayNext();
    }

    public class MyBinder extends Binder implements CallBack {
        @Override
        public boolean isPlayerMusic() {
            return playerMusic();

        }
        @Override
        public int callTotalDate() {
            if (mPlayer != null) {
                return mPlayer.getDuration();
            } else {
                return 0;
            }
        }

        @Override
        public int callCurrentTime() {
            if (mPlayer != null) {
                 return mPlayer.getCurrentPosition();
            } else {
                return 0;
            }
        }

        @Override
        public void iSeekTo(int m_second) {
            if (mPlayer != null) {
                mPlayer.seekTo(m_second);
            }
        }

        @Override
        public void isPlayPre() {
            if (--currentPos < 0) {
                currentPos = 0;
            }
//            //前台显示
//            builder.setContentTitle(musicPathLists.get(currentPos).getArtist()) .setSmallIcon(R.drawable.icon).setContentText(musicPathLists.get(currentPos).getTitle()).setWhen(System.currentTimeMillis());
//            notification = builder.build();
//            mNotificationManager.notify(1,notification);
            //主函数的静态变量值
            MainActivity.value = currentPos;
            initMusic();
            playerMusic();
        }

        @Override
        public void isPlayNext() {

            if (++currentPos > musicPathLists.size() - 1) {
                currentPos = musicPathLists.size() - 1;
            }
//            //前台显示
//            builder.setContentTitle(musicPathLists.get(currentPos).getArtist()) .setSmallIcon(R.drawable.icon).setContentText(musicPathLists.get(currentPos).getTitle()).setWhen(System.currentTimeMillis());
//            notification = builder.build();
//           mNotificationManager.notify(1,notification);
            MainActivity.value = currentPos;
            initMusic();
            playerMusic();
        }

//        @Override
//        public boolean isPlayering() {
//            if(mPlayer.isPlaying()){
//                return true;
//            }else{
//                return false;
//            }
//        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
    }

    private void initMusic() {
        // 根路径
        //      String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmd.mp3";
        mPlayer.reset();
        try {
            mPlayer.setDataSource(musicPathLists.get(currentPos).getMusicPath());
            mPlayer.prepare();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                   //notification = builder.build(); // 获取构建好的Notification
                    //currentPos++;
                    if (currentPos >= musicPathLists.size()) {
                        currentPos = 0;
                    }
                    //       mp.start();
                    MainActivity.value = currentPos;
//                    //前台显示
//                    builder.setContentTitle(musicPathLists.get(currentPos).getArtist()) .setSmallIcon(R.drawable.icon).setContentText(musicPathLists.get(currentPos).getTitle()).setWhen(System.currentTimeMillis());
//                    builder.setPriority(Notification.PRIORITY_HIGH);//高优先级
//                    notification = builder.build();
//                    notification.defaults = Notification.DEFAULT_ALL;

//                    Intent intent = new Intent();
//                    intent.setClass(getApplicationContext(),MainActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                    startActivity(intent);
//                    mNotificationManager.notify(1,notification);
//                    //notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
//                    mNotificationManager.notify(1,notification);
                    initMusic();
                    playerMusic();

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean playerMusic() {
        MainActivity.value = currentPos;
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            return false;
        } else {
            mPlayer.start();
            return true;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//    builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
//    Intent nfIntent = new Intent(this, MusicListActivity.class);
//    builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
//                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.drawable.icon))
//            .setContentTitle("星星制作的播放器") .setSmallIcon(R.drawable.icon).setContentText("播放歌曲名称")
//                .setWhen(System.currentTimeMillis());
//    notification = builder.build(); // 获取构建好的Notification
//    notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
//       // notification.contentView.setTextViewText(R.id.total_time_txt,"当前播放时间");
//       // notification.contentView.setTextViewText(R.id.);
//        startForeground(1, notification);
        //创建一个NotificationManager的引用

//        musicPathLists = intent.getParcelableArrayListExtra("MUSIC_LIST");
//        currentPos = intent.getIntExtra("CURRENT_POSITION", -1);
//        mNotificationManager = (NotificationManager)getSystemService(ns);
//        builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
//        Intent nfIntent = new Intent(this, MainActivity.class);
//        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
//                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.drawable.icon))
//                .setContentTitle(musicPathLists.get(currentPos).getArtist()) .setSmallIcon(R.drawable.icon).setContentText(musicPathLists.get(currentPos).getTitle())
//                .setWhen(System.currentTimeMillis());
//        notification = builder.build(); // 获取构建好的Notification
//        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
//        notification.flags |= FLAG_AUTO_CANCEL;
//        mNotificationManager.notify(1,notification);
//        initMusic();
//        playerMusic();
        return super.onStartCommand(intent, flags, startId);
//        return START_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        musicPathLists = intent.getParcelableArrayListExtra("MUSIC_LIST");
        currentPos = intent.getIntExtra("CURRENT_POSITION", -1);
        initMusic();
        playerMusic();
        return new MyBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.release();
        }
    }
}
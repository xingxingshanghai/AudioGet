package example.com.audioget;

import android.os.Parcel;
import android.os.Parcelable;

public class MusicBean implements Parcelable {
    private String musicName;
    private String musicPath;
    private String image; // icon
    private String artist; // 艺术家
    private int length; // 长度
    private int id; // 音乐id
    private String title; // 音乐标题
    private String updateTime;

    public String getMusicName() {
        return musicName;
    }
    public String getUpdateTime(){return updateTime;}
    public void setUpdateTime(String uptime){ this.updateTime = uptime; }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {this.artist = artist;}

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(musicName);
        dest.writeString(musicPath);
        dest.writeString(image);
        dest.writeString(artist);
        dest.writeInt(length);
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(updateTime);
    }

    /**
     * 必须用 public static final 修饰符
     * 对象必须用 CREATOR
     */
    public static final Creator<MusicBean> CREATOR = new Creator<MusicBean>() {
        @Override
        public MusicBean createFromParcel(Parcel source) {
            MusicBean music = new MusicBean();
            music.setMusicName(source.readString());
            music.setMusicPath(source.readString());
            music.setImage(source.readString());
            music.setArtist(source.readString());
            music.setLength(source.readInt());
            music.setId(source.readInt());
            music.setTitle(source.readString());
            music.setUpdateTime(source.readString());
            return music;
        }
        @Override
        public MusicBean[] newArray(int size) {
            return new MusicBean[size];
        }

    };
}

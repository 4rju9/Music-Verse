package cf.arjun.dev.music_verse;

public class MusicModel {

    private String id, title, artist, path;

    public MusicModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public MusicModel(String id, String title, String artist, String path) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.path = path;
    }
}

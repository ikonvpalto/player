package client.player;

import entity.Track;

import javax.sound.sampled.LineUnavailableException;

public interface Player{

    void open(Track track) throws LineUnavailableException;
    void close();
    void play();
    void pause();
    float getVolume();
    void setVolume(float volume);
    float getPlaybackPos();
    void setPlaybackPos(float pos);

    boolean isOpened();
    boolean isPlaying();

    void addPlaybackListener(PlaybackListener listener);
    void removePlaybackListener(PlaybackListener listener);
}

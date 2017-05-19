package client.player;

import client.player.listener.PlayerListener;
import entity.Track;

import javax.sound.sampled.LineUnavailableException;

public interface MusicPlayer {

    void open(Track track) throws LineUnavailableException;
    void close();
    void resume();
    void pause();

    float getVolume();
    void setVolume(float volume);
    float getPlaybackPos();
    void setPlaybackPos(float pos);

    boolean isOpened();
    boolean isPlaying();

    void addListener(PlayerListener listener);
    void removeListener(PlayerListener listener);
}

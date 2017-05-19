package client.player.impl;

import client.object_pool.ObjectPool;
import client.player.MusicPlayer;
import client.player.listener.PlayerListener;
import entity.Track;
import web.AudioInfo;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;

public class MusicPlayerImpl implements MusicPlayer {

    public static float VOLUME_MIN = 0;
    public static float VOLUME_MAX = 1;

    AudioPlayer player;
    CacheHolder cache;
    ListenersNotifier notifier;

    Thread playerThread;
    Thread cacheThread;
    Thread notifierThread;

    boolean playing;
    boolean opened;
    float volume;
    FloatControl masterGain;

    public MusicPlayerImpl() {
        playing = false;
        opened = false;
        volume = 1f;

        player = new AudioPlayer();
        cache = new CacheHolder();
        notifier = new ListenersNotifier();

        player.cache = cache;
        cache.player = player;
        notifier.musicPlayer = this;
        notifier.cache = cache;
        notifier.player = player;

        playerThread = new Thread(player);
        cacheThread = new Thread(cache);
        notifierThread = new Thread(notifier);

        playerThread.start();
        cacheThread.start();
        notifierThread.start();
    }

    @Override
    public void open(Track track) throws LineUnavailableException {
        if (opened) {
            close();
        }
        cache.stop();
        AudioInfo info = cache.start(track);
        player.start(info.getAudioFormat());
        masterGain = player.getMasterGain();
        masterGain.setValue(
                  masterGain.getMinimum()
                + (masterGain.getMaximum() - masterGain.getMinimum())
                * (float) Math.sqrt(volume));
        opened = true;
    }

    @Override
    public void close() {
        if (playing)
            pause();
        masterGain = null;
        player.stop();
        opened = false;
    }

    @Override
    public void resume() {
        if (!opened || playing)
            return;
        player.resume();
        playing = true;
    }

    @Override
    public void pause() {
        if (!opened || !playing)
            return;
        player.pause();
        playing = false;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public void setVolume(float volume) {
        volume = (float) Math.sqrt(volume);
        this.volume = Float.min(VOLUME_MAX, Float.max(VOLUME_MIN, volume));
        masterGain.setValue(
                  masterGain.getMinimum()
                + (masterGain.getMaximum() - masterGain.getMinimum())
                * volume);
    }

    @Override
    public float getPlaybackPos() {
        return player.playbackPos.get();
    }

    @Override
    public void setPlaybackPos(float pos) {
        player.setPos(pos);
    }

    @Override
    public boolean isOpened() {
        return opened;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void addListener(PlayerListener listener) {
        notifier.addListener(listener);
    }

    @Override
    public void removeListener(PlayerListener listener) {
        notifier.removeListener(listener);
    }

    @Override
    protected void finalize() throws Throwable {
        player.stop();
        notifier.close();
        cache.interrupt();
        super.finalize();
    }
}

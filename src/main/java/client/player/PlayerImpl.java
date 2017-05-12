package client.player;

import entity.Track;
import network.AudioChunk;
import network.AudioInfo;

import javax.sound.sampled.*;
import java.util.LinkedList;
import java.util.List;

public class PlayerImpl implements Player, Runnable {

    private Cache cache;
    private SourceDataLine speakerLine;
    private float volume = 0.7f;
    private List<PlaybackListener> listeners = new LinkedList<>();
    private boolean opened;
    private boolean playing;
    private Thread playingThread = new Thread(this);
    private FloatControl masterGain;
    private float playbackPos;
    private AudioInfo info;

    public PlayerImpl() {
        playingThread.start();
        cache = new CacheImpl();
    }

    @Override
    public void open(Track track) throws LineUnavailableException {
        if (opened) {
            close();
            cache.clear();
        }
        info = cache.loadTrack(track);
        speakerLine = AudioSystem.getSourceDataLine(info.getAudioFormat());
        System.out.println(speakerLine.getBufferSize());
        speakerLine.open();
        speakerLine.start();
        masterGain = (FloatControl) speakerLine.getControl(FloatControl.Type.MASTER_GAIN);
        masterGain.setValue(
                masterGain.getMinimum() +
                (masterGain.getMaximum() - masterGain.getMinimum()) * volume);
        playingThread = new Thread(this);
        playingThread.start();
        opened = true;
    }

    public void printInfo() {
        System.out.println(
                "opened: " + opened + " playing: " + playing + " line opened: " + speakerLine.isOpen() +
                " line active: " + speakerLine.isActive() + " line running: " + speakerLine.isRunning() +
                " playing interrupted: " + playingThread.isInterrupted() + " playing thread state: " + playingThread.getState().toString()
        );
    }

    @Override
    public void close() {
        playing = false;
        masterGain = null;
        speakerLine.flush();
        speakerLine.close();
        playbackPos = 0;
        opened = false;
    }

    @Override
    public void play() {
        if (!opened)
            return;
        playing = true;
    }

    @Override
    public void pause() {
        if (!playing)
            return;
        playing = false;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public void setVolume(float volume) {
        this.volume = Float.max(0, Float.min(1, volume));
        if (opened)
            masterGain.setValue(
                    masterGain.getMinimum() +
                    (masterGain.getMaximum() - masterGain.getMinimum()) * volume);
    }

    @Override
    public float getPlaybackPos() {
        return playbackPos;
    }

    @Override
    public void setPlaybackPos(float pos) {
        playbackPos = Float.max(Float.min(pos, 1), 0);
        speakerLine.flush();
        noticeListeners(PlaybackListener.EventType.PB_PLAY);
    }

    private void noticeListeners(PlaybackListener.EventType eventType) {
        for (PlaybackListener listener : listeners)
            listener.update(eventType, playbackPos);
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
    public void addPlaybackListener(PlaybackListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removePlaybackListener(PlaybackListener listener) {
        listeners.remove(listener);
    }

    private void playOff() {
        playing = false;
        masterGain = null;
        cache.clear();
        speakerLine.flush();
        speakerLine.close();
        playbackPos = 0;
        opened = false;
        noticeListeners(PlaybackListener.EventType.PB_PLAY_OFF);
    }

    @Override
    public void run() {
        int bytes;
        while (!playingThread.isInterrupted()) {
            if (playing) {
                AudioChunk chunk = cache.getChunk(playbackPos);
                if (0 != chunk.getLen()) {
                    speakerLine.write(chunk.getChunk(), 0, chunk.getLen());
                    playbackPos += chunk.getDuration();
                    noticeListeners(PlaybackListener.EventType.PB_PLAY);
                } else {
                    speakerLine.drain();
                    playOff();
                }
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}

package client.player.impl;

import client.object_pool.ObjectPool;
import concurrency.AtomicFloat;
import web.AudioChunk;

import javax.sound.sampled.*;
import java.util.concurrent.atomic.AtomicBoolean;

class AudioPlayer implements Runnable {

    CacheHolder cache;
    AtomicFloat playbackPos;
    SourceDataLine speakerLine;
    AtomicBoolean working;
    AtomicBoolean opened;
    AtomicBoolean playing;
    AtomicBoolean waiting;

    AudioPlayer() {
        playing = new AtomicBoolean(false);
        opened = new AtomicBoolean(false);
        working = new AtomicBoolean(false);
        waiting = new AtomicBoolean(false);
        playbackPos = new AtomicFloat(0);
    }

    @Override
    public void run() {
        working.set(true);
        while (working.get()) {
            if (playing.get()) {
                AudioChunk chunk = cache.readChunk();
                if (null == chunk) {
                    pause();
                    waiting.set(true);
                    boolean end = cache.waitCache();
                    waiting.set(false);
                    if (!end)
                        resume();
                    else {
                        speakerLine.drain();

                    }
                } else
                    speakerLine.write(chunk.getChunk(), 0, chunk.getLen());
            }
        }
    }

    public void stop() {
        speakerLine.flush();
        speakerLine.close();
        speakerLine = null;
        playbackPos.set(0);
        opened.set(false);
    }

    public void start(AudioFormat audioFormat) throws LineUnavailableException {
        speakerLine = AudioSystem.getSourceDataLine(audioFormat);
        speakerLine.open();
        opened.set(true);
    }

    public FloatControl getMasterGain() {
        return (FloatControl) speakerLine.getControl(FloatControl.Type.MASTER_GAIN);
    }

    public void pause() {
        if (!playing.get())
            return;
        speakerLine.stop();
        playing.set(false);
    }

    void resume() {
        if (!opened.get() || playing.get() || waiting.get())
            return;
        playing.set(true);
        speakerLine.start();
    }

    void interrupt() {
        working.set(false);
        stop();
    }

    public void setPos(float pos) {
        pause();
        speakerLine.flush();
        playbackPos.set(pos);
        cache.changePos(pos);
        resume();
    }
}

package client.player.impl;

import client.object_pool.ObjectPool;
import client.web.WebInterface;
import entity.Track;
import web.AudioChunk;
import web.AudioInfo;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

class CacheHolder implements Runnable {

    public static final int WAITING_CHUNKS = 15;

    WebInterface webInterface;
    AudioPlayer player;
    ConcurrentLinkedQueue<AudioChunk> queue;
    AtomicBoolean working;
    AtomicBoolean loading;

    CacheHolder() {
        webInterface = ObjectPool.getPool().getWebInterface();
        queue = new ConcurrentLinkedQueue<>();
        working = new AtomicBoolean(false);
        loading = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        working.set(true);
        while (working.get()) {
            if (loading.get()) {
                try {
                    AudioChunk chunk = webInterface.readChunk();
                    if (chunk.getLen() < 1) {
                        loading.set(false);
                        continue;
                    }
                    queue.add(chunk);
                } catch (Exception e) {
                    loading.set(false);
                }
            }
        }
    }

    void interrupt() {
        working.set(false);
        stop();
    }

    AudioInfo start(Track track) {
        if (loading.get())
            stop();
        AudioInfo info = webInterface.openTrack(track);
        loading.set(true);
        return info;
    }

    void stop() {
        loading.set(false);
        queue.clear();
    }

    AudioChunk readChunk() {
        return queue.poll();
    }

    void changePos(float pos) {
        loading.set(false);
        clearCash();
        webInterface.setPos(pos);
        loading.set(true);
    }

    void clearCash() {
        queue.clear();
    }

    boolean waitCache() {
        while (loading.get() && (queue.size() < WAITING_CHUNKS)) {
            try {
                Thread.sleep(100);
            } catch (Exception ignored) {}
        }
        return !loading.get();
    }
}

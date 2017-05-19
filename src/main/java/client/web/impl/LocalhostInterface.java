package client.web.impl;

import client.object_pool.ObjectPool;
import client.web.WebInterface;
import common.Consts;
import entity.Track;
import web.AudioChunk;
import web.AudioInfo;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalhostInterface implements WebInterface {

    AudioInputStream tmp;
    AtomicInteger num;
    int last;
    long duration;
    double frameRate;
    long frameSize;
    AudioChunk[] buf;

    public LocalhostInterface() {
        num = new AtomicInteger(0);
    }

    @Override
    public AudioInfo openTrack(Track track) {
        try {
            File audioFile = new File(track.getTitle());
            AudioFileFormat format = AudioSystem.getAudioFileFormat(audioFile);
            AudioInputStream in = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat inFormat = in.getFormat();
            AudioFormat decodeFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    inFormat.getSampleRate(),
                    16,
                    inFormat.getChannels(),
                    inFormat.getChannels() * 2,
                    inFormat.getSampleRate(),
                    false);
            tmp = AudioSystem.getAudioInputStream(decodeFormat, in);

            duration = (Long) format.properties().get("duration");
            frameRate = tmp.getFormat().getFrameRate();
            frameSize = tmp.getFormat().getFrameSize();

            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(audioFile);
            AudioInfo info = new AudioInfo();
            info.setAudioFormat(decodeFormat);
            info.setDuration((Long) fileFormat.properties().get("duration"));
            info.setChunksAmount(getNum(1));
            buf = new AudioChunk[getNum(1)];
            num.set(0);
            last = 0;
            return info;
        } catch (Exception e)  {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AudioChunk readChunk() throws IOException {
        while (last <= num.get()) {
            AudioChunk chunk = new AudioChunk();
            byte buf[] = new byte[Consts.PACKAGE_SIZE];
            int bytes = tmp.read(buf);
            this.buf[last] = chunk;
            chunk.setLen(bytes);
            chunk.setChunk(buf);
            chunk.setNum(last++);
        }
        return buf[num.getAndIncrement()];
    }

    @Override
    public void setPos(float pos) {
        num.set(getNum(pos));
    }

    public int getNum(float pos) {
        return (int) (frameSize * duration * pos * frameRate / Consts.PACKAGE_SIZE / 1000000);
    }
}

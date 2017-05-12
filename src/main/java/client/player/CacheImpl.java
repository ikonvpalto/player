package client.player;

import common.Consts;
import entity.Track;
import network.AudioChunk;
import network.AudioInfo;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class CacheImpl implements Cache {

    private AudioChunk[] chunks;
    private AudioInputStream tmp;

    @Override
    public AudioInfo loadTrack(Track track) {
        try {
            File audioFile = new File(track.getTitle());
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
            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(audioFile);
            AudioInfo info = new AudioInfo();
            info.setAudioFormat(decodeFormat);
            info.setChunksAmount((int) (audioFile.getUsableSpace() / Consts.PACKAGE_SIZE_MAX));
            info.setDuration((Long) fileFormat.properties().get("duration"));
            chunks = new AudioChunk[(int) (audioFile.getUsableSpace() / Consts.PACKAGE_SIZE_MAX)];
            return info;
        } catch (Exception e)  {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void clear() {
        chunks = null;
        if (null != tmp) {
            try {
                tmp.close();
            } catch (Exception ignored) {}
            tmp = null;
        }
    }

    @Override
    public AudioChunk getChunk(float pos) {
        int chunkNum = (int) (pos * chunks.length);
        if (chunkNum < 0 || chunkNum >= chunks.length)
            return null;
        if (null != chunks[chunkNum])
            return chunks[chunkNum];
        try {
//            tmp.reset();
//            tmp.skip(chunkNum * Consts.PACKAGE_SIZE_MAX);
            AudioChunk chunk = new AudioChunk();
            chunk.setLen(tmp.read(chunk.getChunk()));
            chunks[chunkNum] = chunk;
            chunk.setDuration(1f / chunks.length);
            return chunk;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

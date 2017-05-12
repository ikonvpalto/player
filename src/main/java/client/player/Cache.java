package client.player;

import entity.Track;
import network.AudioChunk;
import network.AudioInfo;

public interface Cache {

    AudioInfo loadTrack(Track track);
    void clear();
    AudioChunk getChunk(float pos);

}

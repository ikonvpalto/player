package client.web;

import entity.Track;
import web.AudioChunk;
import web.AudioInfo;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;

public interface WebInterface {

    AudioInfo openTrack(Track track);
    AudioChunk readChunk() throws IOException;
    void setPos(float pos);

}

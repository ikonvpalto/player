package web;

import common.Consts;

public class AudioChunk {

    byte[] chunk = new byte[Consts.PACKAGE_SIZE];
    int len;
    private int num;

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public byte[] getChunk() {
        return chunk;
    }

    public void setChunk(byte[] chunk) {
        this.chunk = chunk;
    }

    public void setChunk(byte[] chunk, int bytes) {
        len = Integer.min(Consts.PACKAGE_SIZE_MAX, Integer.max(Consts.PACKAGE_SIZE_MIN, bytes));
        for (int i = 0; i < len; i++) {
            if (i < bytes)
                this.chunk[i] = chunk[i];
            else
                this.chunk[i] = 0;
        }
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public boolean empty() {
        return 0 == len;
    }
}

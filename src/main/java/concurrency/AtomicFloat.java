package concurrency;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicFloat {

    private AtomicInteger value;

    public AtomicFloat(float value) {
        this.value = new AtomicInteger(Float.floatToIntBits(value));
    }

    public float get() {
        return Float.intBitsToFloat(value.get());
    }

    public void set(float value) {
        this.value.set(Float.floatToIntBits(value));
    }
}

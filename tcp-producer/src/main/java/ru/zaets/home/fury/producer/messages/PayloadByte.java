package ru.zaets.home.fury.producer.messages;

import java.util.Random;

/**
 * Created by dzaets on 01.10.2016.
 */
public class PayloadByte implements Payload<byte[]> {

    private final int size;

    public PayloadByte(int size) {
        this.size = size;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public byte[] next() {
        byte[] b = new byte[size];
        new Random().nextBytes(b);
        return b;
    }
}

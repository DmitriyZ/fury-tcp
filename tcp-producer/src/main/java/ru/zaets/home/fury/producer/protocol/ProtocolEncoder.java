package ru.zaets.home.fury.producer.protocol;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * Created by dzaets on 20.04.2016.
 */
public class ProtocolEncoder implements org.apache.mina.filter.codec.ProtocolEncoder {

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        out.write(IoBuffer.wrap((byte[]) message));
    }

    @Override
    public void dispose(IoSession session) throws Exception {

    }
}

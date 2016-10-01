package ru.zaets.home.fury.producer.protocol;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * Created by dzaets on 20.04.2016.
 */
public class ProtocolDecoder extends ProtocolDecoderAdapter {
    @Override
    public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        byte[] dest = new byte[in.remaining()];
        System.arraycopy(in.array(), 0, dest, 0, in.remaining());
        in.flip();
        out.write(dest);
    }
}

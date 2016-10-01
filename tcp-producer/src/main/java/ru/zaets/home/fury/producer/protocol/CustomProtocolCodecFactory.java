package ru.zaets.home.fury.producer.protocol;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;

/**
 * Created by dzaets on 21.04.2016.
 */
public class CustomProtocolCodecFactory implements ProtocolCodecFactory {
    @Override
    public org.apache.mina.filter.codec.ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return new ProtocolEncoder();
    }

    @Override
    public org.apache.mina.filter.codec.ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return new ProtocolDecoder();
    }
}

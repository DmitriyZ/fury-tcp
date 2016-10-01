package ru.zaets.home.fury.producer.handler;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dzaets on 20.04.2016.
 */
public class DefaultClientSessionHandler implements IoHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClientSessionHandler.class);

    @Override
    public void sessionCreated(IoSession session) throws Exception {
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {

    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {

    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        LOGGER.error("Session exception: " + session, cause);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {

    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {
        session.closeNow();
    }
}

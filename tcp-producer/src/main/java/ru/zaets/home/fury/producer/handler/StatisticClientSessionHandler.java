package ru.zaets.home.fury.producer.handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zaets.home.fury.config.FuryConfig;
import ru.zaets.home.fury.producer.client.Client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dzaets on 20.04.2016.
 */
public class StatisticClientSessionHandler extends DefaultClientSessionHandler {

    private static final Logger STATISTIC_RESULT = LoggerFactory.getLogger(StatisticClientSessionHandler.class);
    private static final Logger ERRORS = LoggerFactory.getLogger("DISCONNECTED");

    private static final ConcurrentHashMap<Long, ClientState> clients = new ConcurrentHashMap<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yy.MM.dd HH:mm:ss.S");
    private static final AtomicInteger exceptionCount = new AtomicInteger(0);
    private static int lastSendedMessageCount = 0;

    static {
        final Thread thread = new Thread(() -> {
            int c = 0;
            while (true) {
                if (c++ % 20 == 0) {
                    STATISTIC_RESULT.info("{}|{}|{}|{}|{}|{}|{}|",
                            StringUtils.center("E", 4),
                            StringUtils.center("Timestamp", 21),
                            StringUtils.center("Started", 10),
                            StringUtils.center("Con-ed", 10),
                            StringUtils.center("Data send", 10),
                            StringUtils.center("msg/s", 10),
                            StringUtils.center("Dis-ted", 10)
                    );
                    c = 1;
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignored) {
                }
                final Date now = new Date();
                final int createdClientCount = Client.createdClientCount.get();
                int socketConnected = 0;
                int messageSendCount = 0;
                int disconnected = 0;
                for (ClientState client : clients.values()) {
                    final ClientState.State state = client.state;

                    if (state != ClientState.State.NO && state != ClientState.State.DISCONNECTED) {
                        socketConnected++;
                    }

                    if (state == ClientState.State.DISCONNECTED) {
                        disconnected++;
                    }
                    messageSendCount += client.dataMessageSendCount;
                }

                STATISTIC_RESULT.info("{}|{}|{}|{}|{}|{}|{}|",
                        StringUtils.center(String.valueOf(exceptionCount.get()), 4),
                        StringUtils.center(dateFormat.format(now), 21),
                        StringUtils.center(String.valueOf(createdClientCount), 10),
                        StringUtils.center(String.valueOf(socketConnected), 10),
                        StringUtils.center(String.valueOf(messageSendCount), 10),
                        StringUtils.center(String.valueOf(messageSendCount - lastSendedMessageCount), 10),
                        StringUtils.center(String.valueOf(disconnected), 10)
                );

                lastSendedMessageCount = messageSendCount;

                if (FuryConfig.config.clientCount() == disconnected) {
                    break;
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        clients.computeIfAbsent(session.getId(), id -> {
            final ClientState client = new ClientState();
            client.state = ClientState.State.CONNECTED;
            return client;
        });
        super.sessionOpened(session);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        clients.computeIfPresent(session.getId(), (id, client) -> {
            client.state = ClientState.State.DISCONNECTED;
            return client;
        });
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
        exceptionCount.incrementAndGet();
    }


    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        clients.computeIfPresent(session.getId(), (id, client) -> {
            client.dataMessageSendCount++;
            return client;
        });
        super.messageSent(session, message);
    }

    private static class ClientState {
        State state = State.NO;
        int dataMessageSendCount;

        enum State {
            NO, CONNECTED, DISCONNECTED
        }
    }
}

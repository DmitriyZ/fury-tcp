package ru.zaets.home.fury.producer.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zaets.home.fury.config.FuryConfig;
import ru.zaets.home.fury.producer.messages.Payload;
import ru.zaets.home.fury.producer.session.FuryClientSessionFactory;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Zaets Dmitriy
 */
public class ClientFactory {
    private final static Logger LOGGER = LoggerFactory.getLogger(ClientFactory.class);
    private static ScheduledExecutorService service;

    public static Client createOnlineClient(int id,
                                            FuryClientSessionFactory socketFactory,
                                            int clientPayloadSendInterval,
                                            int clientLifetime,
                                            Payload payload
    ) {
        return new ClientOnline(
                id,
                socketFactory,
                Duration.ofMillis(clientPayloadSendInterval),
                clientLifetime > 0 ? Duration.ofMinutes(clientLifetime) : null,
                payload,
                getScheduledExecutorService()
        );
    }

    public synchronized static ScheduledExecutorService getScheduledExecutorService() {
        if (service == null) {
            final ThreadFactory schedulerThreadFactory = new ThreadFactoryBuilder()
                    .setNameFormat("data-sender-%d")
                    .setDaemon(true)
                    .build();

            int Tsend = 10;
            int count = 8;

            final int sendInterval = FuryConfig.config.clientPayloadSendInterval();
            final int clientCount = FuryConfig.config.clientCount();
            if (sendInterval != 0) {
                int throughput = clientCount * 1000 / sendInterval;
                count = throughput * Tsend / 1000;
                if (count == 0) {
                    count = clientCount < 3 ? 1 : 2;
                } else if (count > 24) {
                    count = 24;
                }
            }

            LOGGER.info("Start ScheduledExecutorService for sending data messages with " + count + " threads.");
            service = new ScheduledThreadPoolExecutor(count, schedulerThreadFactory);
        }
        return service;
    }


}

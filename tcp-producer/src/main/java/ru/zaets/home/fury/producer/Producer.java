package ru.zaets.home.fury.producer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zaets.home.fury.producer.client.Client;
import ru.zaets.home.fury.producer.client.ClientFactory;
import ru.zaets.home.fury.config.FuryConfig;
import ru.zaets.home.fury.producer.handler.DefaultClientSessionHandler;
import ru.zaets.home.fury.producer.handler.StatisticClientSessionHandler;
import ru.zaets.home.fury.producer.messages.PayloadByte;
import ru.zaets.home.fury.producer.protocol.CustomProtocolCodecFactory;
import ru.zaets.home.fury.producer.session.FuryClientSessionFactory;

import java.io.FileNotFoundException;
import java.util.concurrent.*;

import static ru.zaets.home.fury.config.FuryConfig.config;

/**
 * Created by dzaets on 01.10.2016.
 */
public class Producer {
    private static Logger LOGGER = LoggerFactory.getLogger(Producer.class);

    public static void startProducer(FuryConfig.ProducerProperties config) throws InterruptedException, FileNotFoundException {
        LOGGER.info("Start Fury TCP producer!");

        FuryClientSessionFactory socketFactory =
                getFuryProducerClientSocketFactory(
                        config.host(), config.port(),
                        new StatisticClientSessionHandler(),
                        new CustomProtocolCodecFactory()
                );

        LOGGER.info("Start producer with configs: " + config);

        final ThreadFactory executorThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("producer-initializer-%d")
                .setDaemon(true)
                .build();

        final int clientCount = config.clientCount();

        ExecutorService executor = Executors.newFixedThreadPool(clientCount > 16 ? 16 : clientCount, executorThreadFactory);

        final int payloadSendInterval = config.clientPayloadSendInterval();
        final int startupInterval = config.clientStartupInterval();
        final int duration = config.clientLifetime();
        PayloadByte payload = new PayloadByte(config.clientPayloadSize());

        for (int i = 0; i < clientCount; i++) {
            Client Client = ClientFactory.createOnlineClient(i, socketFactory, payloadSendInterval, duration, payload);

            executor.submit(Client);

            if (startupInterval > 0) {
                TimeUnit.MILLISECONDS.sleep(startupInterval);
            }
        }

        executor.shutdown();

        shutdownHooks(socketFactory);

        try {
            if (duration == 0) {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
                final ScheduledExecutorService scheduledExecutorService = ClientFactory.getScheduledExecutorService();
                scheduledExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException: ", e);
        }

        socketFactory.connectorDispose();
    }

    static void shutdownHooks(FuryClientSessionFactory socketFactory) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.warn("Start shutdown Hooks! (dispose connectors)");
            socketFactory.connectorDispose();
            LOGGER.warn("End shutdown Hooks!");
        }, "shutdown-hook"));
    }

    /**
     * Создаем фабрику сокетов.
     *
     * @param handler      хэндлер для сообщений перетающихся по сокету и состояния сессии
     * @param codecFactory фабрика кодеков для кодировани и декодирования исходящих и входящих сообщений соответственно
     * @return фабрика сессий для подключения к КС серверу.
     */
    private static FuryClientSessionFactory getFuryProducerClientSocketFactory(String ip, int port,
                                                                               DefaultClientSessionHandler handler,
                                                                               CustomProtocolCodecFactory codecFactory
    ) {
        return new FuryClientSessionFactory(
                ip,
                port,
                Integer.parseInt(config.getProperty("mina.thread.count")),
                handler,
                codecFactory
        );
    }
}

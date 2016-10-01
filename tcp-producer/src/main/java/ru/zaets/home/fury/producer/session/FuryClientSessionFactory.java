package ru.zaets.home.fury.producer.session;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoServiceStatistics;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zaets.home.fury.config.FuryConfig;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Created by Zaets Dmitriy
 */
public class FuryClientSessionFactory {
    private final static Logger LOGGER = LoggerFactory.getLogger(FuryClientSessionFactory.class);
    private final ProtocolCodecFilter codecFilter;
    private final IoHandler handler;

    private final String host;
    private final int port;

    private IoConnector connector;

    public FuryClientSessionFactory(String host, int port, int threadCount, IoHandler handler, ProtocolCodecFactory codecFactory) {
        this.host = host;
        this.port = port;
        this.handler = handler;
        this.codecFilter = new ProtocolCodecFilter(codecFactory);
        this.connector = initConnector(threadCount);

        startStatistics();
    }


    public synchronized IoSession getSession() throws InterruptedException {
        IoSession session;
        for (; ; ) {
            try {
                LOGGER.info("Try connect for {}:{}...", host, port);
                ConnectFuture future = connector.connect(new InetSocketAddress(host, port));
                future.awaitUninterruptibly(30, TimeUnit.SECONDS);
                session = future.getSession();
                LOGGER.info("Successful connected!");
                break;
            } catch (RuntimeIoException e) {
                LOGGER.error("Failed to connect.", e);
                TimeUnit.SECONDS.sleep(10);
            }
        }
        LOGGER.info("Socket wait until the summation is done.");
        return session;
    }

    public void connectorDispose() {
        connector.dispose();
    }

    private IoConnector initConnector(int threadCount) {
        LOGGER.info("Build Apache Mina NioSocketConnector (threads {})...", threadCount);

        IoConnector connector = new NioSocketConnector(threadCount);
        connector.setConnectTimeoutMillis(TimeUnit.SECONDS.toMillis(1));

        connector.getSessionConfig().setUseReadOperation(true);

        LOGGER.info("Add filter chain to decode and encode messages for connector...");
        connector.getFilterChain().addLast("codec", codecFilter);

        if (FuryConfig.config.enableMinaLogger()) {
            LOGGER.info("Add filter chain to log messages for connector...");
            connector.getFilterChain().addLast("logger", new LoggingFilter());
        }

        connector.getSessionConfig().setThroughputCalculationInterval(1);

        LOGGER.info("Set custom message handler...");
        connector.setHandler(handler);
        return connector;
    }

    private void startStatistics() {
        if (FuryConfig.config.getProperty("mina.enable.statistics").equals("true")) {
            final Thread thread = new Thread(() -> {
                while (!connector.isDisposed()) {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    IoServiceStatistics statistics = connector.getStatistics();
                    statistics.updateThroughput(System.currentTimeMillis());
                    LOGGER.info("------------------------");
                    LOGGER.info("---------WRITE----------");
                    LOGGER.info(String.format("Total write KB: %d, write throughput: %f (KB/s)",
                            statistics.getWrittenBytes() / 1024,
                            statistics.getWrittenBytesThroughput() / 1024)
                    );
                    LOGGER.info(String.format("Total write msgs: %d, write msg throughput: %f (msg/s)",
                            statistics.getWrittenMessages(),
                            statistics.getWrittenMessagesThroughput())
                    );
                    LOGGER.info("---------READ-----------");
                    LOGGER.info(String.format("Total read KB: %d, read throughput: %f (KB/s)",
                            statistics.getReadBytes() / 1024,
                            statistics.getReadBytesThroughput() / 1024)
                    );
                    LOGGER.info(String.format("Total read msgs: %d, read msg throughput: %f (msg/s)",
                            statistics.getReadMessages(),
                            statistics.getReadMessagesThroughput()
                    ));
                }
            });
            thread.setName("Mina Statistics");
            thread.setDaemon(true);
            thread.start();
        }
    }
}

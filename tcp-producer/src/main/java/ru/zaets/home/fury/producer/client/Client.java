package ru.zaets.home.fury.producer.client;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zaets.home.fury.producer.messages.Payload;
import ru.zaets.home.fury.producer.session.FuryClientSessionFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dzaets on 20.04.2016.
 */
public abstract class Client implements Runnable {
    public final static AtomicInteger createdClientCount = new AtomicInteger(0);
    final static Logger LOGGER = LoggerFactory.getLogger(Client.class);
    protected final Payload payload;
    private final ScheduledExecutorService scheduledExecutorService;
    protected final int clientNumber;
    private final FuryClientSessionFactory sessionFactory;

    Client(int clientNumber, FuryClientSessionFactory sessionFactory, Payload payload,
           ScheduledExecutorService scheduledExecutorService) {
        this.clientNumber = clientNumber;
        this.sessionFactory = sessionFactory;
        this.payload = payload;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void run() {
        final Thread currentThread = Thread.currentThread();
        final String oldName = currentThread.getName();
        currentThread.setName("Client-" + clientNumber);
        try {
            createdClientCount.incrementAndGet();
            IoSession session = sessionFactory.getSession();
            doWork(session);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            currentThread.setName(oldName);
        }
    }

    abstract void doWork(IoSession session);

    void asyncSendMessage(Runnable work, int initialDelay, long period, TimeUnit unit) {
        scheduledExecutorService.scheduleAtFixedRate(work, initialDelay, period, unit);
    }

}

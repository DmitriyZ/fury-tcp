package ru.zaets.home.fury.producer.client;

import org.apache.mina.core.session.IoSession;
import ru.zaets.home.fury.producer.messages.Payload;
import ru.zaets.home.fury.producer.session.FuryClientSessionFactory;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Zaets Dmitriy
 */
class ClientOnline extends Client {

    private static final Timer timer = new Timer("Life clientWorkDuration for Clients", true);

    private final TimerTask task;
    private Duration sendInterval;
    private Duration lifetime;
    private AtomicBoolean canSend = new AtomicBoolean(false);

    ClientOnline(int id, FuryClientSessionFactory sessionFactory,
                 Duration clientPayloadSendInterval, Duration clientLifetime,
                 Payload payload,
                 ScheduledExecutorService scheduledExecutorService) {
        super(id, sessionFactory, payload, scheduledExecutorService);
        this.sendInterval = clientPayloadSendInterval;
        this.lifetime = clientLifetime;
        task = new TimerTask() {
            @Override
            public void run() {
                canSend.set(false);
            }
        };
    }


    @Override
    void doWork(IoSession session) {
        LOGGER.info("Start client with lifetime {} sec and send sendInterval {} ms",
                lifetime != null ? lifetime.getSeconds() : "INFINITY",
                sendInterval != null ? sendInterval.toMillis() : "ASAP");
        try {
            while (!session.isConnected()) {
                TimeUnit.MILLISECONDS.sleep(50);
            }
            canSend.set(true);
        } catch (InterruptedException e) {
            LOGGER.error("Some error occurs", e);
            return;
        }

        if (!session.isConnected()) {
            LOGGER.error("Session status is disconnected!");
            return;
        }

        if (lifetime != null) {
            createWorkTimer();
        }

        LOGGER.info("Ready to send data to Host");
        asyncSendMessage(() -> {
            try {
                if (canSend.get() && payload.hasNext() && session.isConnected()) {
                    session.write(payload.next()).await();
                } else {
                    session.getCloseFuture().awaitUninterruptibly();
                }
            } catch (Throwable e) {
                LOGGER.error("Some error occurs with client: " + clientNumber, e);
                task.cancel();
                if (lifetime != null) {
                    LOGGER.info("Time is over: {} sec" + lifetime.getSeconds());
                }
            }
        }, (int) (sendInterval.toMillis() / 5), sendInterval.toMillis(), TimeUnit.MILLISECONDS);

    }

    private void createWorkTimer() {
        timer.schedule(task, lifetime.toMillis());
    }
}

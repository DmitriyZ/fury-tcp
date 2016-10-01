package ru.zaets.home.fury;

import ru.zaets.home.fury.config.FuryConfig;

import static ru.zaets.home.fury.producer.Producer.startProducer;

/**
 * Created by Zaets Dmitriy.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        startProducer(FuryConfig.config);
    }
}

package ru.zaets.home.fury;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zaets.home.fury.config.FuryConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static ru.zaets.home.fury.producer.Producer.startProducer;

/**
 * Created by Zaets Dmitriy
 */
public class MainJar {
    private static Logger LOGGER;
    private static Options options = new Options();

    static {
        options.addOption(Option.builder("h").longOpt("host").desc("target server IP address").hasArg().required().build());
        options.addOption(Option.builder("p").longOpt("port").desc("target server port address").hasArg().required().build());
        options.addOption(Option.builder("c").longOpt("count").desc("count of Fury Producer clients").hasArg().required().build());

        options.addOption(Option.builder().longOpt("client-startup-interval")
                .desc("time between next client start (in milliseconds). Default: 0 - all client start ASAP").hasArg().build());
        options.addOption(Option.builder().longOpt("payload-send-interval")
                .desc("time between send next Payload message by client").hasArg().required().build());
        options.addOption(Option.builder().longOpt("payload-size")
                .desc("message size in bytes").hasArg().required().build());


        options.addOption(Option.builder("pl").longOpt("producer-lifetime").desc("life time of Fury producer").hasArg().required().build());
        options.addOption(Option.builder("cl").longOpt("client-lifetime").desc("clients life time of Fury Producer").hasArg().required().build());


        options.addOption(Option.builder().longOpt("mina-chain-logger")
                .desc("add Apache Mina chain logger for intercept input and output message").build());


        options.addOption(Option.builder("v").desc("Fury version").build());

    }


    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            if (args.length == 1 && args[0].equals("-v")) {
                System.out.println("Fury version : " + furyVersion());
                return;
            }
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            System.err.println();
            final HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.setWidth(120);
            helpFormatter.printHelp("fury-producer", options, true);
            System.exit(1);
        }
        LOGGER = LoggerFactory.getLogger(MainJar.class);
        printJarVersion();

        FuryConfig.config.setCommandLine(line);

        startProducer(FuryConfig.config);
    }

    private static void printJarVersion() {
        LOGGER.info("Fury version : " + furyVersion());
    }

    private static String furyVersion() {
        try {
            InputStream resourceAsStream =
                    MainJar.class.getResourceAsStream(
                            "/META-INF/maven/ru.zaets.home/tcp-producer/pom.properties"
                    );
            Properties prop = new Properties();
            prop.load(resourceAsStream);
            return prop.getProperty("version");
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }
}

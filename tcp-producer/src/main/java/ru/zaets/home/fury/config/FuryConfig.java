package ru.zaets.home.fury.config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.RuntimeIoException;
import ru.zaets.home.fury.Main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Created by Zaets Dmitriy
 */
public class FuryConfig {
    public final static ProducerProperties config = initConfig();

    private static ProducerProperties initConfig() {
        Properties properties = new Properties();
        try {
            InputStream props = null;
            try {
                props = Main.class.getResourceAsStream("/config.properties");
                properties.load(props);
                return new ProducerProperties(properties);
            } finally {
                if (props != null) {
                    props.close();
                }
            }

        } catch (IOException e) {
            throw new RuntimeIoException("can't load properties file", e);
        }
    }


    public static class ProducerProperties extends Properties {
        private CommandLine commandLine;

        ProducerProperties(Properties defaults) {
            super(defaults);
        }

        public void setCommandLine(CommandLine commandLine) {
            this.commandLine = commandLine;
        }

        public boolean enableMinaLogger() {
            if (commandLine != null) {
                return commandLine.hasOption("mina-chain-logger");
            } else {
                return getProperty("mina.chain.add.logger", "false").equals("true");
            }
        }

        public String host() {
            if (commandLine != null) {
                return commandLine.getOptionValue("host");
            } else {
                return getProperty("host");
            }
        }

        public int port() {
            if (commandLine != null) {
                return Integer.parseInt(commandLine.getOptionValue("port"));
            } else {
                return Integer.parseInt(getProperty("port"));
            }
        }

        public int clientCount() {
            if (commandLine != null) {
                return Integer.parseInt(commandLine.getOptionValue("count"));
            } else {
                return Integer.parseInt(getProperty("client.count"));
            }
        }

        public int producerLifeTime() {
            if (commandLine != null) {
                return Integer.parseInt(commandLine.getOptionValue("producer-lifetime", "0"));
            } else {
                return Integer.parseInt(getProperty("producer.lifetime", "0"));
            }
        }

        public int clientStartupInterval() {
            if (commandLine != null) {
                return Integer.parseInt(commandLine.getOptionValue("client-startup-interval", "10"));
            } else {
                return Integer.parseInt(getProperty("client.startupInterval", "10"));
            }
        }

        public int clientLifetime() {
            if (commandLine != null) {
                return Integer.parseInt(commandLine.getOptionValue("client-lifetime", "0"));
            } else {
                return Integer.parseInt(getProperty("client.lifetime", "0"));
            }
        }

        public int clientPayloadSendInterval() {
            if (commandLine != null) {
                return Integer.parseInt(commandLine.getOptionValue("payload-send-interval", "0"));
            } else {
                return Integer.parseInt(getProperty("client.payload.sendInterval", "0"));
            }
        }

        public int clientPayloadSize() {
            if (commandLine != null) {
                return Integer.parseInt(commandLine.getOptionValue("payload-size", "256"));
            } else {
                return Integer.parseInt(getProperty("client.payload.size", "256"));
            }
        }


        @Override
        public String toString() {
            String buf = "";
            if (commandLine != null) {
                final Option[] options = commandLine.getOptions();
                StringBuilder commands = new StringBuilder(64);
                for (Option option : options) {
                    commands
                            .append(StringUtils.rightPad(option.getOpt() + " " + option.getLongOpt(), 40))
                            .append(option.getValue()).append("\n");
                }

                buf = "\nPriority config:\n" +
                        "CommandLine:\n" +
                        commands;

                buf = "\n" + buf;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            super.list(ps);
            return buf + "\n" + new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}

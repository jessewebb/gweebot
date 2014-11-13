/*
 * Copyright (c) 2014 Jesse Webb
 *
 * This file is part of gweebot.
 *
 * gweebot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gweebot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with gweebot.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.jessewebb.gweebot;

import org.apache.commons.cli.*;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.managers.ListenerManager;
import org.pircbotx.hooks.managers.ThreadedListenerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GweeBot {
    private static final Logger logger = LoggerFactory.getLogger(GweeBot.class);

    public static void main(String[] args) {
        logger.info("GweeBot v" + getVersion());

        logger.info("Parsing command-line options");
        Options options = getCommandLineOptions();
        CommandLineParser commandLineParser = new GnuParser();
        CommandLine commandLine = null;
        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            logger.error("Failed to parse command line options", e);
            printCommandLineHelpMessage();
            System.exit(1);
        }

        if (commandLine.hasOption("h")) {
            printCommandLineHelpMessage();
            System.exit(0);
        }

        logger.info("Loading 'gweebot.properties' configuration file");
        Properties props = new Properties();
        InputStream inputStream = GweeBot.class.getResourceAsStream("/gweebot.properties");
        try {
            props.load(inputStream);
        } catch (IOException e) {
            logger.error("Failed to load 'gweebot.properties' configuration file", e);
            System.exit(1);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("Failed to close 'gweebot.properties' InputStream", e);
            }
        }

        String botName = props.getProperty("botname");
        String hostname = props.getProperty("hostname");
        String port = props.getProperty("port");
        String channel = props.getProperty("channel");

        logger.info("GweeBot.botname  = " + botName);
        logger.info("GweeBot.hostname = " + hostname);
        logger.info("GweeBot.port     = " + port);
        logger.info("GweeBot.channel  = " + channel);

        String password = null;
        if (commandLine.hasOption("p")) {
            logger.info("Using password from command line options");
            password = commandLine.getOptionValue("p");
        } else {
            logger.info("Requesting password through System.console()");
            try {
                Console console = System.console();
                if (console == null) throw new RuntimeException("System.console() is null");
                System.out.println("Please enter the password of '" + botName + "'");
                char[] passwordChars = console.readPassword("Password: ");
                password = new String(passwordChars);
            } catch (Exception e) {
                logger.error("Failed to get password through System.console()", e);
                System.exit(1);
            }
        }

        logger.info("Configuring PircBotX");
        ListenerManager<PircBotX> listenerManager = new ThreadedListenerManager<PircBotX>();
        listenerManager.addListener(new CommandListener());
        Configuration<PircBotX> config = new Configuration.Builder<PircBotX>()
                .setName(botName)
                .setLogin(botName)
                .setServer(hostname, Integer.parseInt(port), password)
                .addAutoJoinChannel(channel)
                .setListenerManager(listenerManager)
                .buildConfiguration();
        PircBotX bot = new PircBotX(config);

        logger.info("Connecting GweeBot as '" + botName + "' to '" + hostname + ":" + port + channel + "'");
        try {
            bot.startBot();
        } catch (Exception e) {
            logger.error("Failed to start GweeBot", e);
            System.exit(1);
        }
    }

    private static Options getCommandLineOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "show this message");
        options.addOption("p", "password", true, "password or oauth token");
        return options;
    }

    private static void printCommandLineHelpMessage() {
        HelpFormatter helpformatter = new HelpFormatter();
        helpformatter.printHelp("GweeBot", getCommandLineOptions());
    }

    public static String getVersion() {
        return GweeBot.class.getPackage().getImplementationVersion();
    }
}

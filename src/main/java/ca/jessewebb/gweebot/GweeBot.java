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

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.managers.ListenerManager;
import org.pircbotx.hooks.managers.ThreadedListenerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GweeBot {
    private static final Logger logger = LoggerFactory.getLogger(GweeBot.class);

    public static void main(String[] args) {
        logger.info("Launching GweeBot v" + getVersion());

        Properties props = new Properties();
        InputStream inputStream = GweeBot.class.getResourceAsStream("/gweebot.properties");
        try {
            props.load(inputStream);
        } catch (IOException e) {
            logger.error("BING!", e);
            System.exit(1);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("BING!", e);
            }
        }

        String botName = props.getProperty("botname");
        String hostname = props.getProperty("hostname");
        String port = props.getProperty("port");
        String channel = props.getProperty("channel");
        String password = "oauth:password";

        logger.info("BotName = " + botName);
        logger.info("Channel = " + channel);

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
        try {
            logger.info("Connecting...");
            bot.startBot();
        } catch (Exception e) {
            logger.error("BING!", e);
            System.exit(1);
        }
    }

    public static String getVersion() {
        return GweeBot.class.getPackage().getImplementationVersion();
    }
}

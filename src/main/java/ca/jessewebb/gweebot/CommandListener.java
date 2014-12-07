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

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommandListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CommandListener.class);
    private static final CommandThrottler commandThrottler = buildCommandThrottler();

    private static final String COMMAND_PREFIX  = "!";
    private static final String TIME_COMMAND    = COMMAND_PREFIX + "time";
    private static final String VERSION_COMMAND = COMMAND_PREFIX + "version";

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        if (!event.getMessage().startsWith(COMMAND_PREFIX)) return;

        String message = event.getMessage();
        String username = event.getUser().getNick();
        logger.info("Received command message '{}' from '{}'", message, username);
        
        String command = message.split("\\s+")[0].toLowerCase();

        if (command.equals(TIME_COMMAND)) {
            logger.info("Recognized command '{}'", command);
            if (commandThrottler.throttleCommand(command)) {
                logger.info("Throttled command '{}'", command);
            } else {
                logger.info("Responding to command '{}'", command);
                event.getChannel().send().message(getTimeString());
                logger.info("Tracking usage of command '{}'", command);
                commandThrottler.trackCommandUsage(command);
            }
        } else if (command.equals(VERSION_COMMAND)) {
            logger.info("Recognized command '{}'", command);
            if (commandThrottler.throttleCommand(command)) {
                logger.info("Throttled command '{}'", command);
            } else {
                logger.info("Responding to command '{}'", command);
                event.getChannel().send().action(getVersionString());
                logger.info("Tracking usage of command '{}'", command);
                commandThrottler.trackCommandUsage(command);
            }
        } else {
            logger.info("Unrecognized command '{}'", command);
        }
    }

    private static CommandThrottler buildCommandThrottler() {
        return new CommandThrottler(buildCommandThrottlerConfiguration(), new SystemClock());
    }

    private static CommandThrottlerConfiguration buildCommandThrottlerConfiguration() {
        CommandThrottlerConfiguration configuration = new CommandThrottlerConfiguration();
        configuration.addCommandThrottle(TIME_COMMAND, 10000l);
        configuration.addCommandThrottle(VERSION_COMMAND, 10000l);
        return configuration;
    }

    private String getTimeString() {
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(now);
    }

    private String getVersionString() {
        return "v" + GweeBot.getVersion();
    }
}

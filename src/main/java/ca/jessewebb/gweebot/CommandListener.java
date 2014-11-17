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

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        if (event.getMessage().equalsIgnoreCase("!time")) {
            if (commandThrottler.throttleCommand("!time")) {
                logger.info("Throttled command !time sent from " + event.getUser().getNick());
            } else {
                Date now = new Date();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = dateFormat.format(now);
                event.getChannel().send().message(time);
                commandThrottler.trackCommandUsage("!time");
            }
        } else if (event.getMessage().equalsIgnoreCase("!version")) {
            if (commandThrottler.throttleCommand("!version")) {
                logger.info("Throttled command !version sent from " + event.getUser().getNick());
            } else {
                event.getChannel().send().action("v" + GweeBot.getVersion());
                commandThrottler.trackCommandUsage("!version");
            }
        }
    }

    private static CommandThrottler buildCommandThrottler() {
        return new CommandThrottler(buildCommandThrottlerConfiguration(), new SystemClock());
    }

    private static CommandThrottlerConfiguration buildCommandThrottlerConfiguration() {
        CommandThrottlerConfiguration configuration = new CommandThrottlerConfiguration();
        configuration.addCommandThrottle("!time", 5000l);
        configuration.addCommandThrottle("!version", 10000l);
        return configuration;
    }
}

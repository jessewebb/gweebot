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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommandThrottler {
    private CommandThrottlerConfiguration configuration;
    private EpochClock clock;
    private ConcurrentMap<String, Long> history;

    public CommandThrottler(CommandThrottlerConfiguration configuration, EpochClock clock) {
        if (configuration == null) throw new IllegalArgumentException("configuration must not be null");
        if (clock == null) throw new IllegalArgumentException("clock must not be null");
        this.configuration = configuration;
        this.clock = clock;
        history = new ConcurrentHashMap<String, Long>();
    }

    public void trackCommandUsage(String command) {
        if (command == null) throw new IllegalArgumentException("command must not be null");
        history.put(command, clock.getCurrentEpochTimeMillis());
    }

    public boolean throttleCommand(String command) {
        if (command == null) throw new IllegalArgumentException("command must not be null");
        boolean throttle = false;
        if (configuration.hasCommandThrottle(command) && history.containsKey(command)) {
            long throttleUntil = history.get(command) + configuration.getCommandThrottle(command);
            throttle = clock.getCurrentEpochTimeMillis() < throttleUntil;
        }
        return throttle;
    }
}

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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommandThrottler {
    private Map<String, Long> config;
    private ConcurrentMap<String, Long> history;

    public CommandThrottler() {
        config = new HashMap<String, Long>();
        config.put("!time", 5000l);
        config.put("!version", 10000l);
        history = new ConcurrentHashMap<String, Long>();
    }

    public void trackCommandUsage(String command) {
        history.put(command, System.currentTimeMillis());
    }

    public boolean throttleCommand(String command) {
        boolean throttle = false;
        if (history.containsKey(command)) {
            long throttleUntil = history.get(command) + config.get(command);
            throttle = System.currentTimeMillis() < throttleUntil;
        }
        return throttle;
    }
}

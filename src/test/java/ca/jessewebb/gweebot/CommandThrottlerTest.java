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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandThrottlerTest {

    private static final String TEST_COMMAND = "!test";
    private static final long TEST_COMMAND_THROTTLE_MILLIS = 1000l;

    private CommandThrottlerConfiguration configuration;
    private EpochClock mockClock;
    private CommandThrottler commandThrottler;

    @Before
    public void setUp() {
        configuration = new CommandThrottlerConfiguration();
        configuration.addCommandThrottle(TEST_COMMAND, TEST_COMMAND_THROTTLE_MILLIS);
        mockClock = mock(EpochClock.class);
        when(mockClock.getCurrentEpochTimeMillis()).thenReturn(System.currentTimeMillis());
        commandThrottler = new CommandThrottler(configuration, mockClock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_shouldThrowIllegalArgumentException_whenConfigurationIsNull() {
        new CommandThrottler(null, mockClock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_shouldThrowIllegalArgumentException_whenClockIsNull() {
        new CommandThrottler(configuration, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void trackCommandUsage_shouldThrowIllegalArgumentException_whenCommandIsNull() {
        commandThrottler.trackCommandUsage(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throttleCommand_shouldThrowIllegalArgumentException_whenCommandIsNull() {
        commandThrottler.throttleCommand(null);
    }

    @Test
    public void throttleCommand_shouldReturnFalse_whenCommandIsNotConfiguredToBeThrottled() {
        String notConfiguredCommand = "!not";
        assertFalse(commandThrottler.throttleCommand(notConfiguredCommand));
    }

    @Test
    public void throttleCommand_shouldReturnFalse_whenCommandHasNotBeenUsedYet() {
        assertFalse(commandThrottler.throttleCommand(TEST_COMMAND));
    }

    @Test
    public void throttleCommand_shouldReturnTrue_whenCommandWasUsedJustNow() {
        long mockEpochTimeMillis = System.currentTimeMillis();
        when(mockClock.getCurrentEpochTimeMillis()).thenReturn(mockEpochTimeMillis);
        commandThrottler.trackCommandUsage(TEST_COMMAND);
        assertTrue(commandThrottler.throttleCommand(TEST_COMMAND));
    }

    @Test
    public void throttleCommand_shouldReturnTrue_whenCommandWasUsedVeryRecentlyWithinThrottleTime() {
        long mockEpochTimeMillis = System.currentTimeMillis();
        when(mockClock.getCurrentEpochTimeMillis()).thenReturn(mockEpochTimeMillis);
        commandThrottler.trackCommandUsage(TEST_COMMAND);

        mockEpochTimeMillis += 1l;
        when(mockClock.getCurrentEpochTimeMillis()).thenReturn(mockEpochTimeMillis);
        assertTrue(commandThrottler.throttleCommand(TEST_COMMAND));
    }

    @Test
    public void throttleCommand_shouldReturnTrue_whenCommandWasUsedRecentlyWithinThrottleTime() {
        long mockEpochTimeMillis = System.currentTimeMillis();
        when(mockClock.getCurrentEpochTimeMillis()).thenReturn(mockEpochTimeMillis);
        commandThrottler.trackCommandUsage(TEST_COMMAND);

        mockEpochTimeMillis += TEST_COMMAND_THROTTLE_MILLIS - 1l;
        when(mockClock.getCurrentEpochTimeMillis()).thenReturn(mockEpochTimeMillis);
        assertTrue(commandThrottler.throttleCommand(TEST_COMMAND));
    }

    @Test
    public void throttleCommand_shouldReturnFalse_whenCommandWasUsedAfterThrottleTime() {
        long mockEpochTimeMillis = System.currentTimeMillis();
        when(mockClock.getCurrentEpochTimeMillis()).thenReturn(mockEpochTimeMillis);
        commandThrottler.trackCommandUsage(TEST_COMMAND);

        mockEpochTimeMillis += TEST_COMMAND_THROTTLE_MILLIS;
        when(mockClock.getCurrentEpochTimeMillis()).thenReturn(mockEpochTimeMillis);
        assertFalse(commandThrottler.throttleCommand(TEST_COMMAND));
    }

    @Test
    public void throttleCommand_shouldReturnFalse_whenCommandWasUsedWellAfterThrottleTime() {
        long mockEpochTimeMillis = System.currentTimeMillis();
        when(mockClock.getCurrentEpochTimeMillis()).thenReturn(mockEpochTimeMillis);
        commandThrottler.trackCommandUsage(TEST_COMMAND);

        mockEpochTimeMillis += TEST_COMMAND_THROTTLE_MILLIS + 1l;
        when(mockClock.getCurrentEpochTimeMillis()).thenReturn(mockEpochTimeMillis);
        assertFalse(commandThrottler.throttleCommand(TEST_COMMAND));
    }
}

package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top cards of the controller's library, grants permission to play them until end of
 * turn, and puts time counters on the exiled cards that have suspend.
 */
public record ExileTopCardsMayPlayThisTurnAndPutTimeCountersOnSuspendedCardsEffect(
        int count,
        int timeCounters
) implements CardEffect {

    public ExileTopCardsMayPlayThisTurnAndPutTimeCountersOnSuspendedCardsEffect {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
        if (timeCounters <= 0) {
            throw new IllegalArgumentException("timeCounters must be positive");
        }
    }
}

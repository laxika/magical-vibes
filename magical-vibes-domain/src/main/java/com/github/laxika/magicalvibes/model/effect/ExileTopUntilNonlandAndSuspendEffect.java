package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles cards from the controller's library until a nonland card is found, then suspends that
 * card with the specified number of time counters.
 */
public record ExileTopUntilNonlandAndSuspendEffect(int timeCounters) implements CardEffect {

    public ExileTopUntilNonlandAndSuspendEffect {
        if (timeCounters < 1) {
            throw new IllegalArgumentException("timeCounters must be positive");
        }
    }
}

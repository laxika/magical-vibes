package com.github.laxika.magicalvibes.model.effect;

/** Exiles cards from the controller's library until a nonland card is found and suspends it. */
public record ExileTopUntilNonlandWithSuspendEffect(int timeCounters) implements CardEffect {

    public ExileTopUntilNonlandWithSuspendEffect {
        if (timeCounters < 1) {
            throw new IllegalArgumentException("timeCounters must be positive");
        }
    }
}

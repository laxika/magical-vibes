package com.github.laxika.magicalvibes.model.effect;

/** Exiles the dying source card with time counters and registers it for suspend processing. */
public record ExileSourceCardWithSuspendCountersEffect(int timeCounters) implements CardEffect {

    public ExileSourceCardWithSuspendCountersEffect {
        if (timeCounters < 1) {
            throw new IllegalArgumentException("timeCounters must be positive");
        }
    }
}

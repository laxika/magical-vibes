package com.github.laxika.magicalvibes.model.effect;

/** Marks the triggering spell for exile with suspend time counters after it resolves. */
public record ExileTriggeringSpellWithSuspendCountersOnResolutionEffect(int timeCounters)
        implements CardEffect {

    public ExileTriggeringSpellWithSuspendCountersOnResolutionEffect {
        if (timeCounters < 1) {
            throw new IllegalArgumentException("timeCounters must be positive");
        }
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Gives the triggering creature spell an additional counter as it enters the battlefield. */
public record GrantAdditionalCounterToTriggeringCreatureSpellEffect(CounterType counterType,
                                                                     int count)
        implements CardEffect {

    public GrantAdditionalCounterToTriggeringCreatureSpellEffect {
        if (counterType == null) {
            throw new IllegalArgumentException("Counter type is required");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("Counter count must be positive");
        }
    }
}

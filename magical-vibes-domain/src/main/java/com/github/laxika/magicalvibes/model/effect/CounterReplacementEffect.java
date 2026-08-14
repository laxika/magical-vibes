package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Replacement behavior for counters put on permanents controlled by the effect's controller.
 */
public interface CounterReplacementEffect extends CardEffect {

    int replace(CounterType counterType, int count);

    default boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature) {
        return true;
    }
}

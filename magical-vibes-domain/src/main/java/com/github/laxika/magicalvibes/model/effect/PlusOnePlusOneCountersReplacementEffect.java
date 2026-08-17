package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Replacement behavior for +1/+1 counters put on permanents controlled by the effect's
 * controller.
 */
public interface PlusOnePlusOneCountersReplacementEffect extends CounterReplacementEffect {

    int replace(int count);

    @Override
    default int replace(CounterType counterType, int count) {
        return replace(count);
    }

    @Override
    default boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature) {
        return affectedPermanentIsCreature && counterType == CounterType.PLUS_ONE_PLUS_ONE;
    }

    /**
     * Returns whether this replacement also applies to a noncreature Vehicle.
     */
    default boolean appliesToNonCreatureVehicles() {
        return false;
    }
}

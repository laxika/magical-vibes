package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Replacement behavior for counters put on permanents controlled by the effect's controller.
 */
public interface CounterReplacementEffect extends CardEffect {

    int replace(CounterType counterType, int count);

    /**
     * Whether this replacement applies to counters put on permanents regardless of who controls
     * the affected permanent. The default preserves the controller-scoped behavior of existing
     * replacement effects such as Doubling Season.
     */
    default boolean appliesToAllPermanents() {
        return false;
    }

    default boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature) {
        return true;
    }

    default boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature,
                              boolean affectedPermanentIsArtifact) {
        return appliesTo(counterType, affectedPermanentIsCreature);
    }
}

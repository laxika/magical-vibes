package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Activation cost that removes a player-chosen positive number of counters of the given type
 * from the source permanent. The chosen number is carried by the activation's xValue.
 */
public record RemoveOneOrMoreCountersFromSourceCost(CounterType counterType) implements CostEffect {

    public RemoveOneOrMoreCountersFromSourceCost() {
        this(CounterType.PLUS_ONE_PLUS_ONE);
    }
}

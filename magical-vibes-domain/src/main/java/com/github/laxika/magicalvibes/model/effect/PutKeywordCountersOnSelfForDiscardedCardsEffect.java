package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;

/**
 * Puts one counter of each listed keyword-counter type on the source permanent when one or more
 * cards discarded during the triggering discard event have the corresponding keyword.
 */
public record PutKeywordCountersOnSelfForDiscardedCardsEffect(List<CounterType> counterTypes)
        implements CardEffect {

    public PutKeywordCountersOnSelfForDiscardedCardsEffect {
        counterTypes = List.copyOf(counterTypes);
    }
}

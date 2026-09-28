package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;

/**
 * For each listed keyword found on a creature card in the controller's graveyard, puts one
 * matching keyword counter on a creature that controller chooses, then puts that many +1/+1
 * counters on the source.
 */
public record PutKeywordCountersOnControlledCreaturesThenPutPlusOneCountersOnSourceEffect(
        List<CounterType> counterTypes) implements CardEffect {

    public PutKeywordCountersOnControlledCreaturesThenPutPlusOneCountersOnSourceEffect {
        counterTypes = List.copyOf(counterTypes);
    }
}

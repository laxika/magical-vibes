package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Lets the controller put a creature from their hand onto the battlefield, giving it counters
 * as it enters when its mana cost contains X.
 */
public record PutCreatureFromHandOntoBattlefieldWithCountersIfManaCostHasXEffect(
        DynamicAmount counterCount) implements CardEffect {

    public CounterType counterType() {
        return CounterType.PLUS_ONE_PLUS_ONE;
    }
}

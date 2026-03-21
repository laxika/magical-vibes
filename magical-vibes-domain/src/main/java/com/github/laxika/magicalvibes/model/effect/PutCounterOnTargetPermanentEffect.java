package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Puts a counter of the specified type on the targeted permanent.
 * For lore counters on Sagas, this also triggers the appropriate chapter ability
 * per MTG Rule 714.3b.
 */
public record PutCounterOnTargetPermanentEffect(CounterType counterType) implements CardEffect {

    @Override
    public boolean canTargetPermanent() { return true; }
}

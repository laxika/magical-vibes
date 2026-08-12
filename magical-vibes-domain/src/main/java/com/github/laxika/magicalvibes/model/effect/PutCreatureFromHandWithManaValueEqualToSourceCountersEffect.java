package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Lets the controller put a creature card from their hand onto the battlefield when its mana
 * value equals the number of a counter type on the source permanent.
 */
public record PutCreatureFromHandWithManaValueEqualToSourceCountersEffect(CounterType counterType)
        implements CardEffect {
}

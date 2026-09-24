package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Lets the controller put an artifact card from their hand onto the battlefield when its mana
 * value is at most the number of the specified counters on the source permanent.
 */
public record PutArtifactFromHandWithManaValueAtMostSourceCountersEffect(CounterType counterType)
        implements CardEffect {
}

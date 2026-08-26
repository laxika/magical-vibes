package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Offers an artifact spell from hand whose mana value is at most the resolution-time count of a
 * counter type on the source permanent.
 */
public record MayCastArtifactFromHandWithManaValueAtMostSourceCountersEffect(CounterType counterType)
        implements CardEffect {
}

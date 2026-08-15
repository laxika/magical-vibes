package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Moves any number of one kind of counter from other permanents controlled by the ability's
 * controller onto the source permanent.
 *
 * @param counterType the kind of counter to move
 */
public record MoveCountersFromControlledPermanentsToSourceEffect(CounterType counterType)
        implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Moves any number of counters from the source permanent onto other creatures.
 *
 * @param counterType the kind of counter that may be moved
 */
public record MoveCountersFromSourceToOtherCreaturesEffect(CounterType counterType)
        implements CardEffect {
}

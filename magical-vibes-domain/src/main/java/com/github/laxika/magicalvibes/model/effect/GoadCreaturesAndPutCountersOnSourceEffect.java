package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Goads matching creatures until the controller's next turn, then puts counters on the source. */
public record GoadCreaturesAndPutCountersOnSourceEffect(
        CounterType counterType,
        PermanentPredicate affectedPredicate
) implements CardEffect {
}

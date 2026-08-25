package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * As-enters choice to return a matching permanent to its owner's hand and have the entering
 * permanent enter with counters if the return succeeds.
 *
 * @param filter the permanents that may be returned
 * @param counterType the counter type put on the entering permanent
 * @param counterCount the number of counters put on the entering permanent
 * @param permanentDescription the permanent description used in the choice prompt
 */
public record MayReturnPermanentToHandAndEnterWithCountersEffect(
        PermanentPredicate filter,
        CounterType counterType,
        int counterCount,
        String permanentDescription
) implements ReplacementEffect {
}

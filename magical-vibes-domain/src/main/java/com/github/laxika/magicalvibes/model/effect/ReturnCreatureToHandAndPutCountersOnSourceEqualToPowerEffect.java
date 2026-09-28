package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Returns one matching creature to its owner's hand, then puts +1/+1 counters on the source
 * equal to that creature's effective power before it leaves the battlefield.
 */
public record ReturnCreatureToHandAndPutCountersOnSourceEqualToPowerEffect(
        PermanentPredicate filter,
        String permanentDescription
) implements CardEffect {
}

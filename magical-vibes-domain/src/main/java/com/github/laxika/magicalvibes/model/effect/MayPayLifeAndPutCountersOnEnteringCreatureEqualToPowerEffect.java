package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may pay life equal to its power. If you do, put that many +1/+1 counters on it" (Madame Null): "it" is the
 * stack entry's triggering permanent, its power read as the ability resolves, and the counters are as many as the
 * life paid.
 */
public record MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect() implements CardEffect {
}

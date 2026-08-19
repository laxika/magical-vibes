package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "Whenever a creature you control [...] enters, [you may] put N counters of a specified type on it."
 *
 * <p>Trigger-materialising marker for {@code ON_ALLY_CREATURE_ENTERS_BATTLEFIELD}. Unlike a plain
 * {@link PutCounterOnTargetPermanentEffect} this does not target — "it" is the creature that just
 * entered. The enter collector resolves the entering permanent and, when {@code optional}, queues a
 * {@code MayEffect(PutCounterOnTargetPermanentEffect(counterType, count))} with {@code targetId}
 * set to that creature and {@code sourcePermanentId} set to this permanent; when not {@code optional}
 * it queues the mandatory counter placement directly. Typically wrapped in an
 * {@link EnteringCreatureMinPowerConditionalEffect} (Mighty Emergence: count = 2, minPower = 5, may) or
 * an {@link EnteringCreatureExactStatsConditionalEffect} (Sigil Captain: count = 2, is 1/1, mandatory).
 */
public record PutCountersOnEnteringCreatureEffect(CounterType counterType, int count, boolean optional,
                                                  CounterType requiredCounterType)
        implements CardEffect {

    /** The existing +1/+1-counter form (optional). */
    public PutCountersOnEnteringCreatureEffect(int count) {
        this(CounterType.PLUS_ONE_PLUS_ONE, count, true, null);
    }

    /** The existing +1/+1-counter form. */
    public PutCountersOnEnteringCreatureEffect(int count, boolean optional) {
        this(CounterType.PLUS_ONE_PLUS_ONE, count, optional, null);
    }

    /** A counter-placement form without a resolution-time counter condition. */
    public PutCountersOnEnteringCreatureEffect(CounterType counterType, int count, boolean optional) {
        this(counterType, count, optional, null);
    }
}

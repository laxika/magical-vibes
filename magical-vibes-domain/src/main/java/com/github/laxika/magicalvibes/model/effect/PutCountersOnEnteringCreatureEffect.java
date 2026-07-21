package com.github.laxika.magicalvibes.model.effect;

/**
 * "Whenever a creature you control [...] enters, [you may] put N +1/+1 counters on it."
 *
 * <p>Trigger-materialising marker for {@code ON_ALLY_CREATURE_ENTERS_BATTLEFIELD}. Unlike a plain
 * {@link PutCounterOnTargetPermanentEffect} this does not target — "it" is the creature that just
 * entered. The enter collector resolves the entering permanent and, when {@code optional}, queues a
 * {@code MayEffect(PutCounterOnTargetPermanentEffect(PLUS_ONE_PLUS_ONE, count))} with {@code targetId}
 * set to that creature and {@code sourcePermanentId} set to this permanent; when not {@code optional}
 * it queues the mandatory counter placement directly. Typically wrapped in an
 * {@link EnteringCreatureMinPowerConditionalEffect} (Mighty Emergence: count = 2, minPower = 5, may) or
 * an {@link EnteringCreatureExactStatsConditionalEffect} (Sigil Captain: count = 2, is 1/1, mandatory).
 */
public record PutCountersOnEnteringCreatureEffect(int count, boolean optional) implements CardEffect {

    /** "You may put N +1/+1 counters on it" (optional). */
    public PutCountersOnEnteringCreatureEffect(int count) {
        this(count, true);
    }
}

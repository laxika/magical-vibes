package com.github.laxika.magicalvibes.model.effect;

/**
 * "Whenever a creature you control [...] enters, you may put N +1/+1 counters on it."
 *
 * <p>Trigger-materialising marker for {@code ON_ALLY_CREATURE_ENTERS_BATTLEFIELD}. Unlike a plain
 * {@link PutCounterOnTargetPermanentEffect} this does not target — "it" is the creature that just
 * entered. The enter collector resolves the entering permanent and queues a
 * {@code MayEffect(PutCounterOnTargetPermanentEffect(PLUS_ONE_PLUS_ONE, count))} with {@code targetId}
 * set to that creature and {@code sourcePermanentId} set to this permanent. Typically wrapped in an
 * {@link EnteringCreatureMinPowerConditionalEffect} for the "with power N or greater" gate. Used by
 * Mighty Emergence (count = 2, minPower = 5).
 */
public record PutCountersOnEnteringCreatureEffect(int count) implements CardEffect {
}

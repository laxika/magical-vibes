package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller may pay X, then a reflexive ability moves X +1/+1 counters from the source
 * creature onto another target creature.
 *
 * <p>The target is chosen only after the payment, so this effect itself is not targeted.</p>
 */
public record PayXManaMoveCountersFromSourceToTargetCreatureEffect() implements CardEffect {
}

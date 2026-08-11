package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed triggered ability that resolves {@code effect} whenever a creature dies for
 * the rest of the turn. The delayed ability outlives the source card and is cleared at turn cleanup.
 *
 * @param effect the effect resolved for each creature that dies
 */
public record RegisterDelayedCreatureDeathTriggerEffect(CardEffect effect) implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger that returns the source Aura from its graveyard at the beginning
 * of the next end step, then lets its existing Aura-return handler choose a legal creature to
 * enchant.
 */
public record RegisterDelayedReturnSourceAuraToCreatureEffect() implements CardEffect {
}

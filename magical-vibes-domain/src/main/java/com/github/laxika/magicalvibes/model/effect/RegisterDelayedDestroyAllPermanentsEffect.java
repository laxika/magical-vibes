package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger that destroys all permanents at the beginning of the next end step.
 * The wipe is put onto the stack when that end step begins, so it uses the permanents present when
 * it resolves.
 */
public record RegisterDelayedDestroyAllPermanentsEffect() implements CardEffect {
}

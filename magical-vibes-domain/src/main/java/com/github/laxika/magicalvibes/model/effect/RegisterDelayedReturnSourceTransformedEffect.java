package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger to return the source card from its owner's graveyard
 * to the battlefield transformed at the beginning of the next end step.
 */
public record RegisterDelayedReturnSourceTransformedEffect() implements CardEffect {
}

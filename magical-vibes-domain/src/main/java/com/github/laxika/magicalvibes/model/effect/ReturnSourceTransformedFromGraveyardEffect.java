package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the source card from its owner's graveyard to the battlefield transformed,
 * under the trigger controller's control, immediately on resolution
 * ("When this creature dies, return it to the battlefield transformed under your control.").
 *
 * <p>Unlike {@link RegisterDelayedReturnSourceTransformedEffect} there is no wait for the
 * next end step.
 */
public record ReturnSourceTransformedFromGraveyardEffect() implements CardEffect {
}

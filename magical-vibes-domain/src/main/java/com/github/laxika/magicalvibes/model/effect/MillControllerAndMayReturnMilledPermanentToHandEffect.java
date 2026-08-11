package com.github.laxika.magicalvibes.model.effect;

/**
 * Mills cards from the controller's library, then offers each permanent card milled by this
 * resolution for return to its owner's hand. The offers are represented by
 * {@link ReturnMilledPermanentToHandEffect} marker effects.
 */
public record MillControllerAndMayReturnMilledPermanentToHandEffect(int count) implements CardEffect {
}

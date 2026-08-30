package com.github.laxika.magicalvibes.model.effect;

/**
 * Mills one card from the controller's library, records whether it was a Lesson, and offers a
 * milled land card for return to the controller's hand.
 */
public record MillControllerAndMayReturnMilledLandToHandEffect() implements CardEffect {
}

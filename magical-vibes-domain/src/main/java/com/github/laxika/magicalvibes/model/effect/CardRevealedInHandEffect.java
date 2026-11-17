package com.github.laxika.magicalvibes.model.effect;

/**
 * A card revealed in its owner's hand for a while (CR 701.20a): the opponents see it. It is the payload of a floating
 * effect on the card ({@code FloatingContinuousEffect#onCard}) whose controller is the owner of the hand, so it ends
 * at its duration or as the card leaves the hand (CR 400.7).
 */
public record CardRevealedInHandEffect() implements CardEffect {
}

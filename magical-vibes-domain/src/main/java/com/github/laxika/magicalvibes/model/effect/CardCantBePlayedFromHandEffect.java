package com.github.laxika.magicalvibes.model.effect;

/**
 * A card that its owner can't play from their hand for a while. It is the payload of a floating effect on the card
 * ({@code FloatingContinuousEffect#onCard}) whose controller is the owner of the hand, so it ends at its duration or as
 * the card leaves the hand (CR 400.7).
 */
public record CardCantBePlayedFromHandEffect() implements CardEffect {
}

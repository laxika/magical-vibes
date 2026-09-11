package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered effect that mills the player whose life-loss event caused the trigger for a number of
 * cards equal to the life they lost. Used by cards like Mindcrank and The Master of Lake-town.
 * The effect is placed in the opponent- or controller-life-loss slot according to the card's wording.
 * The amount is provided at trigger time via the trigger context, not stored in the effect.
 */
public record MillOpponentOnLifeLossEffect() implements CardEffect {
}

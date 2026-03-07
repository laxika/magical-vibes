package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered effect that mills an opponent for a number of cards equal to the life they lost.
 * Used by cards like Mindcrank.
 * The amount is provided at trigger time via the trigger context, not stored in the effect.
 */
public record MillOpponentOnLifeLossEffect() implements CardEffect {
}

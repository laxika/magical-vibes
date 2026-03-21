package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered effect that draws cards equal to the amount of life gained.
 * Used by cards like Lich's Mastery ("Whenever you gain life, draw that many cards.").
 * The draw amount is provided at trigger time via the trigger context, not stored in the effect.
 */
public record DrawCardsEqualToLifeGainedEffect() implements CardEffect {
}

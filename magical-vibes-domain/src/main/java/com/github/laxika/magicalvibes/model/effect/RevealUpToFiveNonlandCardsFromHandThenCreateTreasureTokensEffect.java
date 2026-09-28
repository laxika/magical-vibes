package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals up to five nonland cards from the controller's hand, then creates a Treasure token for
 * each revealed card whose mana value is shared by another revealed card.
 */
public record RevealUpToFiveNonlandCardsFromHandThenCreateTreasureTokensEffect() implements CardEffect {
}

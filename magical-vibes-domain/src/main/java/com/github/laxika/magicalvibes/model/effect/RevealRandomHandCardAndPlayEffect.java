package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals a card at random from the target player's hand. If it's a land card,
 * the player puts it onto the battlefield. Otherwise, the player casts it
 * without paying its mana cost if able.
 */
public record RevealRandomHandCardAndPlayEffect() implements CardEffect {
}

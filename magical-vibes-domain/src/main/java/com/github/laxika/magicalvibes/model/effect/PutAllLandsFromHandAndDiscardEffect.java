package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the controller's hand, puts every land card from it onto the battlefield untapped at
 * once, then discards the cards that remain in hand.
 */
public record PutAllLandsFromHandAndDiscardEffect() implements CardEffect {
}

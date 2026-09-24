package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library. If it is a nonland card with odd mana value,
 * the controller may cast it without paying its mana cost; otherwise, the controller draws it.
 */
public record RevealTopCardIfOddMayCastFreeElseDrawEffect() implements CardEffect {
}

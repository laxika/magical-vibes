package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the controller's top library card, makes them lose life equal to its mana value, and
 * lets them play it until end of turn.
 */
public record ExileTopCardLoseLifeEqualToManaValueAndMayPlayThisTurnEffect() implements CardEffect {
}

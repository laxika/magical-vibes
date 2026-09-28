package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles cards from the controller's library until a nonland card is found, then offers that card
 * to be cast without paying its mana cost. All exiled cards remain in exile.
 */
public record ExileTopUntilNonlandMayCastWithoutPayingManaEffect() implements CardEffect {
}

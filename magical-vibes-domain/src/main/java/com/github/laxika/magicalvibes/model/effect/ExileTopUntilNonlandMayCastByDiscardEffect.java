package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles cards from the controller's library until a nonland card is found, then offers that card
 * as a free cast by discarding a card rather than paying its mana cost. Intermediate lands and a
 * declined card remain in exile.
 */
public record ExileTopUntilNonlandMayCastByDiscardEffect() implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles cards from the controller's library until a nonland card is found, then offers that card
 * for a free cast. All cards exiled this way remain exiled if the offer is declined.
 */
public record ExileTopUntilNonlandMayCastWithoutPayingManaCostEffect() implements CardEffect {
}

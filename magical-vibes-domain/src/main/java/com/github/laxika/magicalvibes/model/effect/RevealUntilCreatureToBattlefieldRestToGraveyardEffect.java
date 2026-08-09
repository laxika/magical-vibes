package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals cards from the top of the controller's library until a creature card is revealed. That
 * creature is put onto the battlefield under the controller's control, and all other revealed
 * cards are put into the controller's graveyard. If the library is exhausted without revealing a
 * creature, every revealed card goes to the graveyard.
 *
 * <p>Used by Gamekeeper.
 */
public record RevealUntilCreatureToBattlefieldRestToGraveyardEffect() implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals cards from the top of the controller's library until a land card is revealed. That land
 * is put onto the battlefield under the controller's control, and all other revealed cards are put
 * into the controller's graveyard. If the library is exhausted without revealing a land, every
 * revealed card is put into the graveyard.
 */
public record RevealUntilLandToBattlefieldRestToGraveyardEffect() implements CardEffect {
}

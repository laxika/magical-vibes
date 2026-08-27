package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles a random permanent card from the controller's graveyard and creates a tapped token copy
 * of it. If the exiled card is a land, the process repeats.
 */
public record ExileRandomPermanentFromGraveyardCreateTappedTokenCopyEffect() implements CardEffect {
}

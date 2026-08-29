package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the controller a turn-scoped replacement effect that exiles cards that would be put
 * into that player's graveyard.
 */
public record ExileOwnCardsInsteadOfGraveyardUntilEndOfTurnEffect() implements CardEffect {
}

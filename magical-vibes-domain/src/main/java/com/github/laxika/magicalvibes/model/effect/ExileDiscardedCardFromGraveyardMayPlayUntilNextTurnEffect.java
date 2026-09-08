package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the exact card carried by a discard trigger from its owner's graveyard and grants the
 * trigger controller permission to play it until the end of that player's next turn.
 */
public record ExileDiscardedCardFromGraveyardMayPlayUntilNextTurnEffect() implements CardEffect {
}

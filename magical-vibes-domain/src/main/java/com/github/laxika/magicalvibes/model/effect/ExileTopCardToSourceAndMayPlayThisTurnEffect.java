package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the controller's library, tracks it with the source permanent, and lets
 * that controller play it until end of turn.
 */
public record ExileTopCardToSourceAndMayPlayThisTurnEffect() implements CardEffect {
}

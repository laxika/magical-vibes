package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a 0/0 green and blue Fractal creature token and puts X +1/+1 counters on it,
 * where X is the number of cards the controller has drawn this turn
 * ({@code GameData.cardsDrawnThisTurn}). Used by Fractal Anomaly.
 */
public record CreateFractalTokenWithCountersFromCardsDrawnThisTurnEffect() implements CardEffect {
}

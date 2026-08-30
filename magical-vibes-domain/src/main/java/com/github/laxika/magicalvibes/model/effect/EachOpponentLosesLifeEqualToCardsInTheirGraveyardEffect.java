package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent loses life equal to the number of cards in that opponent's graveyard.
 * The amount is evaluated separately for each opponent.
 */
public record EachOpponentLosesLifeEqualToCardsInTheirGraveyardEffect() implements CardEffect {
}

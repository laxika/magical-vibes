package com.github.laxika.magicalvibes.model.effect;

/**
 * During resolution, choose a card from an opponent's graveyard and exile it. Then exile any
 * number of cards with that name from its owner's graveyard, hand, and library, and that player
 * draws a card for each card exiled from their hand this way.
 */
public record ExileCardFromOpponentGraveyardAndSameNameFromZonesEffect() implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces this spell's casting cost by the given generic amount if one or more cards left the
 * controller's graveyard this turn. Used by Wilt in the Heat ({2} less).
 */
public record ReduceOwnCastCostIfCardsLeftGraveyardThisTurnEffect(int amount) implements CardEffect {
}

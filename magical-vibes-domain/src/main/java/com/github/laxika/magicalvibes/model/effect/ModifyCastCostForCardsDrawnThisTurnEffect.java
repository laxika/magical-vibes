package com.github.laxika.magicalvibes.model.effect;

/**
 * Modifies the generic cost of spells cast from cards drawn this turn by the source permanent's
 * controller or by one of that player's opponents.
 */
public record ModifyCastCostForCardsDrawnThisTurnEffect(int amount, boolean opponentsCards)
        implements CardEffect {
}

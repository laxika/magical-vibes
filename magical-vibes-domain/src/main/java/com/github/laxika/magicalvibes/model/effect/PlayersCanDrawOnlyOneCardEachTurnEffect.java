package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: each player can't draw more than one card each turn.
 * Used by Spirit of the Labyrinth.
 */
public record PlayersCanDrawOnlyOneCardEachTurnEffect() implements DrawRestrictionEffect {

    @Override
    public boolean preventsDraw(int cardsDrawnThisTurn) {
        return cardsDrawnThisTurn >= 1;
    }
}

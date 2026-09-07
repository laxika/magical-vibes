package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Static effect: the source controller's opponents can't draw more than one card each turn. */
public record OpponentsCanDrawOnlyOneCardEachTurnEffect() implements DrawRestrictionEffect {

    @Override
    public boolean appliesTo(UUID sourceControllerId, UUID drawingPlayerId) {
        return !sourceControllerId.equals(drawingPlayerId);
    }

    @Override
    public boolean preventsDraw(int cardsDrawnThisTurn) {
        return cardsDrawnThisTurn >= 1;
    }
}

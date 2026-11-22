package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Static effect: the players {@code players} names can't draw more than one card each turn: the source controller's
 * opponents by default, or each player ({@link Subjects#eachPlayer()}, Spirit of the Labyrinth).
 *
 * @param players who is restricted, judged as the restriction is checked
 */
public record OpponentsCanDrawOnlyOneCardEachTurnEffect(PlayerSubject players) implements DrawRestrictionEffect {

    public OpponentsCanDrawOnlyOneCardEachTurnEffect {
        if (!(players instanceof PlayerSubject.Each)) {
            throw new IllegalArgumentException("Unsupported players for a static restriction: " + players);
        }
    }

    public OpponentsCanDrawOnlyOneCardEachTurnEffect() {
        this(Subjects.eachOpponent());
    }

    @Override
    public boolean appliesTo(UUID sourceControllerId, UUID drawingPlayerId) {
        return players.equals(Subjects.eachPlayer()) || !sourceControllerId.equals(drawingPlayerId);
    }

    @Override
    public boolean preventsDraw(int cardsDrawnThisTurn) {
        return cardsDrawnThisTurn >= 1;
    }
}

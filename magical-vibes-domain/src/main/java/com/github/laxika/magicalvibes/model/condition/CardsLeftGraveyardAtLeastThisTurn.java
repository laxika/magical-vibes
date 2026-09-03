package com.github.laxika.magicalvibes.model.condition;

/** The controller had at least {@code minimum} cards leave their graveyard this turn. */
public record CardsLeftGraveyardAtLeastThisTurn(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return minimum + " or more cards left your graveyard this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minimum + " cards left your graveyard this turn";
    }
}

package com.github.laxika.magicalvibes.model.condition;

/** At least one opponent of the controller has drawn {@code minimum} or more cards this turn. */
public record OpponentDrewAtLeastCardsThisTurn(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return "an opponent drew " + minimum + " or more cards this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent drew " + minimum + " or more cards this turn";
    }
}

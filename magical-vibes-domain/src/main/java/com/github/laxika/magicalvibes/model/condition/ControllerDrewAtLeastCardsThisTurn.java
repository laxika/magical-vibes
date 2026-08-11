package com.github.laxika.magicalvibes.model.condition;

/** The controller has drawn at least {@code minimum} cards this turn. */
public record ControllerDrewAtLeastCardsThisTurn(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return minimum + " or more cards drawn this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minimum + " cards drawn this turn";
    }
}

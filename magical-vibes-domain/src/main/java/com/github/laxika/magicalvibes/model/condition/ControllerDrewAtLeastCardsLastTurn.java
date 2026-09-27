package com.github.laxika.magicalvibes.model.condition;

/** The controller drew at least {@code minimum} cards during the immediately preceding turn. */
public record ControllerDrewAtLeastCardsLastTurn(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return minimum + " or more cards drawn last turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minimum + " cards drawn last turn";
    }
}

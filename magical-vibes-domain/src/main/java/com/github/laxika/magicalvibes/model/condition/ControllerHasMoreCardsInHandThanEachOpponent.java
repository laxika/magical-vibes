package com.github.laxika.magicalvibes.model.condition;

/** True when the controller has strictly more cards in hand than every opponent. */
public record ControllerHasMoreCardsInHandThanEachOpponent() implements Condition {

    @Override
    public String conditionName() {
        return "you have more cards in hand than each opponent";
    }

    @Override
    public String conditionNotMetReason() {
        return "you do not have more cards in hand than each opponent";
    }
}

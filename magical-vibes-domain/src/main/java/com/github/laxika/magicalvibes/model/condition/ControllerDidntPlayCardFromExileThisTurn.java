package com.github.laxika.magicalvibes.model.condition;

/** The controller has not played a card from exile this turn. */
public record ControllerDidntPlayCardFromExileThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "you didn't play a card from exile this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you played a card from exile this turn";
    }
}

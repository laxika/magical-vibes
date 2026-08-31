package com.github.laxika.magicalvibes.model.condition;

/** At least one card has been put into exile during the current turn. */
public record CardPutIntoExileThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a card was put into exile this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no card was put into exile this turn";
    }
}

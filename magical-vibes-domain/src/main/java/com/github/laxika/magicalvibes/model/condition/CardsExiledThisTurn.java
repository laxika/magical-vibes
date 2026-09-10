package com.github.laxika.magicalvibes.model.condition;

/** One or more cards were put into exile during the current turn. */
public record CardsExiledThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a card was put into exile this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no card was put into exile this turn";
    }
}

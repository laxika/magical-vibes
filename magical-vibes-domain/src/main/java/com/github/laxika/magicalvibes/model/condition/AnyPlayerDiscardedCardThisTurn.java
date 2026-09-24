package com.github.laxika.magicalvibes.model.condition;

/** At least one player discarded a card this turn. */
public record AnyPlayerDiscardedCardThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a player discarded a card this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no player discarded a card this turn";
    }
}

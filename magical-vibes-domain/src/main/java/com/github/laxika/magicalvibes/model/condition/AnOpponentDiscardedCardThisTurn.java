package com.github.laxika.magicalvibes.model.condition;

/** At least one opponent of the effect's controller discarded a card this turn. */
public record AnOpponentDiscardedCardThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent discarded a card this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent discarded a card this turn";
    }
}

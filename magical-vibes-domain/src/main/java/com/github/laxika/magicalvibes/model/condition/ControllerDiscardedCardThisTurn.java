package com.github.laxika.magicalvibes.model.condition;

/** The effect's controller discarded a card this turn. */
public record ControllerDiscardedCardThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "you discarded a card this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you didn't discard a card this turn";
    }
}

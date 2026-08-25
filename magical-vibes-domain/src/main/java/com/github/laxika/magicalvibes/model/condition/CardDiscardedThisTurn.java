package com.github.laxika.magicalvibes.model.condition;

/** The source card was discarded or cycled by its controller this turn. */
public record CardDiscardedThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "mayhem";
    }

    @Override
    public String conditionNotMetReason() {
        return "this card wasn't discarded this turn";
    }
}

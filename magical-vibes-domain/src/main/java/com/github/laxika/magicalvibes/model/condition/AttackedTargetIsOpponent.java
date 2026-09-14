package com.github.laxika.magicalvibes.model.condition;

/** Whether the attacking creature's target is an opponent rather than a permanent. */
public record AttackedTargetIsOpponent() implements Condition {

    @Override
    public String conditionName() {
        return "attacked target is an opponent";
    }

    @Override
    public String conditionNotMetReason() {
        return "attacked target is not an opponent";
    }
}

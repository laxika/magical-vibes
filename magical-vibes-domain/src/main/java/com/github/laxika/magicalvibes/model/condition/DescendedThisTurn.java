package com.github.laxika.magicalvibes.model.condition;

/** A permanent card was put into the controller's graveyard from anywhere this turn. */
public record DescendedThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "descended this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no permanent card was put into your graveyard this turn";
    }
}

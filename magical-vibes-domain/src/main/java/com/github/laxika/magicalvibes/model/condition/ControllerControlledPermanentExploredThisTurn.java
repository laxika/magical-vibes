package com.github.laxika.magicalvibes.model.condition;

/** A permanent the controller controlled explored at least once this turn. */
public record ControllerControlledPermanentExploredThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a permanent you controlled explored this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no permanent you controlled explored this turn";
    }
}

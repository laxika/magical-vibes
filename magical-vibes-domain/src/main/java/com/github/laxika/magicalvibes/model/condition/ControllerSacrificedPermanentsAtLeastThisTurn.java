package com.github.laxika.magicalvibes.model.condition;

/** The effect's controller sacrificed at least {@code minimum} permanents this turn. */
public record ControllerSacrificedPermanentsAtLeastThisTurn(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return minimum + " or more permanents sacrificed this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minimum + " permanents sacrificed this turn";
    }
}

package com.github.laxika.magicalvibes.model.condition;

/** The controller has played at least {@code minimum} lands this turn. */
public record ControllerPlayedAtLeastLandsThisTurn(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return minimum + " or more lands played this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minimum + " lands played this turn";
    }
}

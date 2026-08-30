package com.github.laxika.magicalvibes.model.condition;

/** At least the given number of distinct sources controlled by the controller dealt damage this turn. */
public record ControllerControlledSourcesDealtDamageThisTurn(int minimumSources) implements Condition {

    @Override
    public String conditionName() {
        return minimumSources + " or more sources you controlled dealt damage this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minimumSources + " sources you controlled dealt damage this turn";
    }
}

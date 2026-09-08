package com.github.laxika.magicalvibes.model.condition;

/** The controller has at least the specified number of energy counters. */
public record ControllerEnergyAtLeast(int threshold) implements Condition {

    public ControllerEnergyAtLeast {
        if (threshold < 0) {
            throw new IllegalArgumentException("Energy threshold cannot be negative");
        }
    }

    @Override
    public String conditionName() {
        return "energy threshold (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " energy counters";
    }
}

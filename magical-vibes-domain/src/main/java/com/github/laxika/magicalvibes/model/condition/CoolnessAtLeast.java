package com.github.laxika.magicalvibes.model.condition;

/** The controller has at least the specified percentage of coolness. */
public record CoolnessAtLeast(int threshold) implements Condition {

    public CoolnessAtLeast {
        if (threshold < 0) {
            throw new IllegalArgumentException("Coolness threshold cannot be negative");
        }
    }

    @Override
    public String conditionName() {
        return "coolness threshold (" + threshold + "%+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "less than " + threshold + "% cool";
    }
}

package com.github.laxika.magicalvibes.model.condition;

/** The controller has at least the specified amount of unspent mana. */
public record ControllerUnspentManaAtLeast(int threshold) implements Condition {

    public ControllerUnspentManaAtLeast {
        if (threshold < 0) {
            throw new IllegalArgumentException("Unspent mana threshold cannot be negative");
        }
    }

    @Override
    public String conditionName() {
        return "unspent mana threshold (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " unspent mana";
    }
}

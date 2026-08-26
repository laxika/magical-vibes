package com.github.laxika.magicalvibes.model.condition;

/** The controller's graveyard contains at least {@code threshold} distinct permanent types. */
public record PermanentTypesInGraveyardAtLeast(int threshold) implements Condition {

    public PermanentTypesInGraveyardAtLeast {
        if (threshold < 0) {
            throw new IllegalArgumentException("Permanent type threshold cannot be negative");
        }
    }

    @Override
    public String conditionName() {
        return "at least " + threshold + " permanent types in graveyard";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " permanent types in graveyard";
    }
}

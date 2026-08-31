package com.github.laxika.magicalvibes.model.condition;

/** The controller's graveyard contains at least {@code threshold} distinct mana values. */
public record DistinctManaValuesAmongCardsInGraveyardAtLeast(int threshold) implements Condition {

    public DistinctManaValuesAmongCardsInGraveyardAtLeast {
        if (threshold < 0) {
            throw new IllegalArgumentException("Mana value threshold cannot be negative");
        }
    }

    @Override
    public String conditionName() {
        return "at least " + threshold + " distinct mana values in graveyard";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " distinct mana values in graveyard";
    }
}

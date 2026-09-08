package com.github.laxika.magicalvibes.model.condition;

/**
 * The total number of cards in exile is at least {@code threshold}.
 */
public record CardsInExileAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "cards in exile (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " cards in exile";
    }
}

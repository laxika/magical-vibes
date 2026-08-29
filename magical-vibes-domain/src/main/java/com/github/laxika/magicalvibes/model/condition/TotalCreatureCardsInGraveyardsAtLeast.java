package com.github.laxika.magicalvibes.model.condition;

/**
 * All players' graveyards contain at least {@code threshold} creature cards in total.
 */
public record TotalCreatureCardsInGraveyardsAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "at least " + threshold + " creature cards in all graveyards";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " creature cards in all graveyards";
    }
}

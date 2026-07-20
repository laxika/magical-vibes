package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller has at most {@code threshold} cards in their hand — "as long as you have
 * {threshold} or fewer cards in hand" (Thresher Lizard).
 */
public record CardsInHandAtMost(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "cards in hand (" + threshold + " or fewer)";
    }

    @Override
    public String conditionNotMetReason() {
        return "more than " + threshold + " cards in hand";
    }
}

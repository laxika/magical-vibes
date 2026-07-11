package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller has at least {@code threshold} cards in their hand (Imaginary Pet).
 */
public record CardsInHandAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "cards in hand (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " cards in hand";
    }
}

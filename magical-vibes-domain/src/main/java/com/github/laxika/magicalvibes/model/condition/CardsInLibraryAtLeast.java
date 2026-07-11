package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller has at least {@code threshold} cards in their library (Battle of Wits).
 */
public record CardsInLibraryAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "cards in library (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " cards in library";
    }
}

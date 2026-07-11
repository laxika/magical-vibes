package com.github.laxika.magicalvibes.model.condition;

/**
 * Some player's library has at most {@code threshold} cards in it (Shelldock Isle).
 */
public record AnyLibraryAtMost(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "a library has " + threshold + " or fewer cards";
    }

    @Override
    public String conditionNotMetReason() {
        return "no library has " + threshold + " or fewer cards";
    }
}

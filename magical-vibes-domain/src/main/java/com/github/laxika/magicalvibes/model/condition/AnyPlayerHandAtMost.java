package com.github.laxika.magicalvibes.model.condition;

/**
 * At least one player has {@code threshold} or fewer cards in hand.
 */
public record AnyPlayerHandAtMost(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "a player has " + threshold + " or fewer cards in hand";
    }

    @Override
    public String conditionNotMetReason() {
        return "each player has more than " + threshold + " cards in hand";
    }
}

package com.github.laxika.magicalvibes.model.condition;

/** True when the defending player has at most the specified number of cards in hand. */
public record DefendingPlayerHandAtMost(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "defending player has at most " + threshold + " cards in hand";
    }

    @Override
    public String conditionNotMetReason() {
        return "defending player has more than " + threshold + " cards in hand";
    }
}

package com.github.laxika.magicalvibes.model.condition;

/**
 * The active player (the player whose turn/step it is) has {@code threshold} or more cards in
 * hand. The counterpart of {@link ActivePlayerHandEmpty} for upkeep triggers that gate on a large
 * hand — "if that player has five or more cards in hand" during each opponent's upkeep resolves to
 * the active player (Misers' Cage).
 */
public record ActivePlayerHandAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return threshold + "+ cards in hand";
    }

    @Override
    public String conditionNotMetReason() {
        return "the player has fewer than " + threshold + " cards in hand";
    }
}

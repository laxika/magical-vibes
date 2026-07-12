package com.github.laxika.magicalvibes.model.condition;

/**
 * The active player (the player whose turn/step it is) has no cards in hand. Used as the
 * intervening-"if" for upkeep triggers that gate on the empty hand of whoever's upkeep it is —
 * "if you have no cards in hand" (own upkeep) and "if that player has no cards in hand" (each
 * opponent's upkeep) both resolve to the active player during the relevant upkeep (Hollowborn
 * Barghest).
 */
public record ActivePlayerHandEmpty() implements Condition {

    @Override
    public String conditionName() {
        return "empty hand";
    }

    @Override
    public String conditionNotMetReason() {
        return "the player has cards in hand";
    }
}

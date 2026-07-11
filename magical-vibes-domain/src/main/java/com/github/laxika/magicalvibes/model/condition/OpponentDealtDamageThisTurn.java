package com.github.laxika.magicalvibes.model.condition;

/**
 * An opponent of the controller has been dealt at least {@code minimumAmount} damage this turn
 * (from any source). Use {@code 1} for "an opponent was dealt damage this turn" (Bloodcrazed Goblin);
 * higher thresholds for cards like Spinerock Knoll ("7 or more damage").
 */
public record OpponentDealtDamageThisTurn(int minimumAmount) implements Condition {

    @Override
    public String conditionName() {
        return minimumAmount <= 1
                ? "an opponent was dealt damage this turn"
                : "an opponent was dealt " + minimumAmount + " or more damage this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return minimumAmount <= 1
                ? "no opponent was dealt damage this turn"
                : "no opponent was dealt " + minimumAmount + " or more damage this turn";
    }
}

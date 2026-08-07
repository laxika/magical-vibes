package com.github.laxika.magicalvibes.model.condition;

/**
 * The source permanent has dealt at least {@code minimumAmount} damage this turn, to any recipient —
 * players, planeswalkers, battles or creatures, in combat or out of it (Chandra, Fire of Kaladesh).
 * Backed by the per-source damage totals in {@code GameData.damageDealtThisTurnBySource}.
 */
public record SelfDealtDamageThisTurn(int minimumAmount) implements Condition {

    @Override
    public String conditionName() {
        return "has dealt " + minimumAmount + " or more damage this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "it has dealt less than " + minimumAmount + " damage this turn";
    }
}

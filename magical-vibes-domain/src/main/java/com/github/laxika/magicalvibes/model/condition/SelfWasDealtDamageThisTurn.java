package com.github.laxika.magicalvibes.model.condition;

/**
 * The source permanent was dealt at least {@code minimumAmount} damage this turn. Backed by
 * {@code GameData.damageDealtToPermanentsThisTurn}; prevented damage is excluded, and the total
 * survives effects that remove marked damage, such as regeneration.
 */
public record SelfWasDealtDamageThisTurn(int minimumAmount) implements Condition {

    public SelfWasDealtDamageThisTurn() {
        this(1);
    }

    @Override
    public String conditionName() {
        return minimumAmount <= 1
                ? "was dealt damage this turn"
                : "was dealt " + minimumAmount + " or more damage this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return minimumAmount <= 1
                ? "it wasn't dealt damage this turn"
                : "it wasn't dealt " + minimumAmount + " or more damage this turn";
    }
}

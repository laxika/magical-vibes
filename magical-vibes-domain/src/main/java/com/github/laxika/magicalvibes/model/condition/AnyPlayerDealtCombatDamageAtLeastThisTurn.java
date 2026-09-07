package com.github.laxika.magicalvibes.model.condition;

/** At least one player was dealt the requested amount of combat damage this turn. */
public record AnyPlayerDealtCombatDamageAtLeastThisTurn(int minimumAmount) implements Condition {

    @Override
    public String conditionName() {
        return minimumAmount <= 1
                ? "a player was dealt combat damage this turn"
                : "a player was dealt " + minimumAmount + " or more combat damage this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return minimumAmount <= 1
                ? "no player was dealt combat damage this turn"
                : "no player was dealt " + minimumAmount + " or more combat damage this turn";
    }
}

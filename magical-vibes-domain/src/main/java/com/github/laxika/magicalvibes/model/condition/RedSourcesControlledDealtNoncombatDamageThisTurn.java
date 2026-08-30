package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller's red sources have dealt at least {@code minimumAmount} noncombat damage this
 * turn.
 */
public record RedSourcesControlledDealtNoncombatDamageThisTurn(int minimumAmount) implements Condition {

    @Override
    public String conditionName() {
        return "red sources you controlled dealt " + minimumAmount
                + " or more noncombat damage this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "red sources you controlled did not deal " + minimumAmount
                + " or more noncombat damage this turn";
    }
}

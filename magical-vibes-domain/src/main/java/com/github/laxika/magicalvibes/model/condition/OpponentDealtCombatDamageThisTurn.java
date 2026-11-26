package com.github.laxika.magicalvibes.model.condition;

/**
 * An opponent of the controller was dealt combat damage this turn: at least one opponent counts for
 * {@code OpponentsDealtCombatDamageThisTurn} ("X, where X is the number of opponents that were dealt combat damage this
 * turn" is at least 1).
 */
public record OpponentDealtCombatDamageThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent was dealt combat damage this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent was dealt combat damage this turn";
    }
}

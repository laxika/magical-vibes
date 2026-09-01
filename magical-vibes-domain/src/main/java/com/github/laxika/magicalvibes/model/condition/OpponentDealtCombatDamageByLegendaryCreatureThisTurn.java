package com.github.laxika.magicalvibes.model.condition;

/** An opponent of the controller was dealt combat damage by a legendary creature this turn. */
public record OpponentDealtCombatDamageByLegendaryCreatureThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent was dealt combat damage by a legendary creature this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent was dealt combat damage by a legendary creature this turn";
    }
}

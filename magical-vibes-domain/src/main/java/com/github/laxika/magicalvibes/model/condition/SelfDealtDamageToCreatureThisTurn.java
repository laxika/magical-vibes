package com.github.laxika.magicalvibes.model.condition;

/** The source permanent dealt damage to a creature this turn. */
public record SelfDealtDamageToCreatureThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "it dealt damage to a creature this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "it dealt no damage to a creature this turn";
    }
}

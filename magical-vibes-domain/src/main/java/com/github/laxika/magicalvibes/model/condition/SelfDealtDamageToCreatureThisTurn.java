package com.github.laxika.magicalvibes.model.condition;

/** True when the source permanent dealt damage to another creature this turn. */
public record SelfDealtDamageToCreatureThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "dealt damage to another creature this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "it dealt no damage to another creature this turn";
    }
}

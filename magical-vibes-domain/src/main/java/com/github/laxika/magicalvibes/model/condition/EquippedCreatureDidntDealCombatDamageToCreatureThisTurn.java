package com.github.laxika.magicalvibes.model.condition;

/** The source is attached to a creature that dealt no combat damage to a creature this turn. */
public record EquippedCreatureDidntDealCombatDamageToCreatureThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "the equipped creature didn't deal combat damage to a creature this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "the equipped creature dealt combat damage to a creature this turn";
    }
}

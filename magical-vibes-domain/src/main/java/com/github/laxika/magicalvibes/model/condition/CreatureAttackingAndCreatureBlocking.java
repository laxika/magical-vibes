package com.github.laxika.magicalvibes.model.condition;

/** At least one creature is attacking and at least one creature is blocking. */
public record CreatureAttackingAndCreatureBlocking() implements Condition {

    @Override
    public String conditionName() {
        return "a creature is attacking and a creature is blocking";
    }

    @Override
    public String conditionNotMetReason() {
        return "no creature is attacking or no creature is blocking";
    }
}

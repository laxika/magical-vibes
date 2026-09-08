package com.github.laxika.magicalvibes.model.condition;

/**
 * The mana spent to cast the triggering spell is greater than the source permanent's current power.
 */
public record SpellManaSpentGreaterThanSourcePower() implements Condition {

    @Override
    public String conditionName() {
        return "mana spent greater than source power";
    }

    @Override
    public String conditionNotMetReason() {
        return "mana spent was not greater than source power";
    }
}

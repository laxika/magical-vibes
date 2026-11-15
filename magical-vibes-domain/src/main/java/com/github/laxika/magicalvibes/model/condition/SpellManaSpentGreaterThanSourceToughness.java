package com.github.laxika.magicalvibes.model.condition;

/**
 * The mana spent to cast the triggering spell (what it recorded as it became cast, read from the condition context's
 * triggering spell) is greater than the source permanent's current toughness.
 */
public record SpellManaSpentGreaterThanSourceToughness() implements Condition {

    @Override
    public String conditionName() {
        return "mana spent greater than source toughness";
    }

    @Override
    public String conditionNotMetReason() {
        return "mana spent was not greater than source toughness";
    }
}

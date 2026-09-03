package com.github.laxika.magicalvibes.model.condition;

/** The targeted spell was cast with less mana spent than its mana value. */
public record TargetSpellManaSpentLessThanManaValue() implements Condition {

    @Override
    public String conditionName() {
        return "target spell was cast for less than its mana value";
    }

    @Override
    public String conditionNotMetReason() {
        return "the amount of mana spent to cast the target spell was not less than its mana value";
    }
}

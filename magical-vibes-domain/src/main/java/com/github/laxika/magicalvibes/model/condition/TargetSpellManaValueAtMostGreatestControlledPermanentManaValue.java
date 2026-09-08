package com.github.laxika.magicalvibes.model.condition;

/** The targeted spell's mana value is at most the greatest mana value among your permanents. */
public record TargetSpellManaValueAtMostGreatestControlledPermanentManaValue() implements Condition {

    @Override
    public String conditionName() {
        return "target spell's mana value is at most the greatest mana value among permanents you control";
    }

    @Override
    public String conditionNotMetReason() {
        return "target spell's mana value is greater than the greatest mana value among permanents you control";
    }
}

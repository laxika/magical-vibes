package com.github.laxika.magicalvibes.model.condition;

/** The targeted permanent's mana value is at most the number of colors spent to cast the spell. */
public record TargetPermanentManaValueAtMostColorsSpent() implements Condition {

    @Override
    public String conditionName() {
        return "target mana value at most colors spent";
    }

    @Override
    public String conditionNotMetReason() {
        return "target mana value exceeds colors spent";
    }
}

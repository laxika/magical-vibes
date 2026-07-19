package com.github.laxika.magicalvibes.model.condition;

/**
 * The resolving spell/ability's chosen {@code X} (snapshotted onto the stack entry's
 * {@code xValue}) is at least {@code minX}. Martial Coup: "If X is 5 or more, destroy all
 * other creatures."
 */
public record SpellXAtLeast(int minX) implements Condition {

    @Override
    public String conditionName() {
        return "X is " + minX + " or more";
    }

    @Override
    public String conditionNotMetReason() {
        return "X is less than " + minX;
    }
}

package com.github.laxika.magicalvibes.model.condition;

/** The permanent was cast without spending mana to cast it, or was not cast. */
public record NoManaSpentToCast() implements Condition {

    @Override
    public String conditionName() {
        return "no mana spent to cast";
    }

    @Override
    public String conditionNotMetReason() {
        return "mana was spent to cast it";
    }
}

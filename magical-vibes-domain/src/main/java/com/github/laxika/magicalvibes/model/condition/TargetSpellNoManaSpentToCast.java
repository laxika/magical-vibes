package com.github.laxika.magicalvibes.model.condition;

/** The targeted spell was cast without spending mana, or is an uncast spell copy. */
public record TargetSpellNoManaSpentToCast() implements Condition {

    @Override
    public String conditionName() {
        return "no mana spent to cast target spell";
    }

    @Override
    public String conditionNotMetReason() {
        return "mana was spent to cast the target spell";
    }
}

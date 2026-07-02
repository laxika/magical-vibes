package com.github.laxika.magicalvibes.model.condition;

/**
 * At least {@code minMana} mana was spent to cast the triggering spell. The amount is
 * snapshotted onto the stack entry's {@code xValue} at trigger time.
 */
public record SpellManaSpentAtLeast(int minMana) implements Condition {

    @Override
    public String conditionName() {
        return minMana + "+ mana spent on cast spell";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minMana + " mana was spent to cast that spell";
    }
}

package com.github.laxika.magicalvibes.model.condition;

/**
 * At least {@code minMana} mana was spent to cast the triggering spell: what the spell recorded as it became cast
 * ({@code StackEntry.manaSpentToCastThisSpell()}), read from the condition context's triggering spell as the ability
 * triggers and as it resolves (the entry's snapshot of the spell, so the same once it has left the stack).
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

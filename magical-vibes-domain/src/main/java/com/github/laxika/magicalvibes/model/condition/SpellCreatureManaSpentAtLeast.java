package com.github.laxika.magicalvibes.model.condition;

/** At least the specified amount of mana produced by creatures was spent to cast the triggering spell. */
public record SpellCreatureManaSpentAtLeast(int minMana) implements Condition {

    @Override
    public String conditionName() {
        return minMana + "+ creature mana spent on cast spell";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minMana + " mana from creatures was spent to cast that spell";
    }
}

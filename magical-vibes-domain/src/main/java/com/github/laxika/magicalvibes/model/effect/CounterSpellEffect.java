package com.github.laxika.magicalvibes.model.effect;

public record CounterSpellEffect() implements CardEffect {
    @Override public boolean canTargetSpell() { return true; }
}

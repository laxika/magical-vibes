package com.github.laxika.magicalvibes.model.effect;

public record CounterSpellAndExileEffect() implements CardEffect {
    @Override public boolean canTargetSpell() { return true; }
}

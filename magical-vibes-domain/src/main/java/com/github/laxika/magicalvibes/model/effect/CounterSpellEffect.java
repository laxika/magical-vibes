package com.github.laxika.magicalvibes.model.effect;

public record CounterSpellEffect() implements CounterSpellingEffect {
    @Override public boolean canTargetSpell() { return true; }
}

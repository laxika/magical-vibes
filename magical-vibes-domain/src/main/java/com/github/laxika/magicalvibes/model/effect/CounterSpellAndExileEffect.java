package com.github.laxika.magicalvibes.model.effect;

public record CounterSpellAndExileEffect() implements CounterSpellingEffect {
    @Override public boolean canTargetSpell() { return true; }
}

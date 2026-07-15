package com.github.laxika.magicalvibes.model.effect;

public record CounterSpellEffect() implements CounterSpellingEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.SPELL_ON_STACK); }
}

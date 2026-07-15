package com.github.laxika.magicalvibes.model.effect;

public record CounterSpellAndExileEffect() implements CounterSpellingEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.SPELL_ON_STACK); }
}

package com.github.laxika.magicalvibes.model.effect;

public record CopySpellEffect() implements CardEffect {
    @Override public boolean canTargetSpell() { return true; }
}

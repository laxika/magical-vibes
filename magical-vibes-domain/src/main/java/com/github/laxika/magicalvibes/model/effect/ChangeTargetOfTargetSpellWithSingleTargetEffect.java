package com.github.laxika.magicalvibes.model.effect;

public record ChangeTargetOfTargetSpellWithSingleTargetEffect() implements CardEffect {
    @Override public boolean canTargetSpell() { return true; }
}

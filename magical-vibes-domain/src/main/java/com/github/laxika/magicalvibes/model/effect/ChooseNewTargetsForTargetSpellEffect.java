package com.github.laxika.magicalvibes.model.effect;

public record ChooseNewTargetsForTargetSpellEffect() implements CardEffect {
    @Override public boolean canTargetSpell() { return true; }
}

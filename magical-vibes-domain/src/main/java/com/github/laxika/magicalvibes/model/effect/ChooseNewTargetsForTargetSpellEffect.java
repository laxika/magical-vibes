package com.github.laxika.magicalvibes.model.effect;

public record ChooseNewTargetsForTargetSpellEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.SPELL_ON_STACK); }
}

package com.github.laxika.magicalvibes.model.effect;

public record ChooseNewTargetsForTargetSpellEffect(boolean mustChangeAllTargets) implements CardEffect {
    public ChooseNewTargetsForTargetSpellEffect() {
        this(false);
    }
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.spellOnStack()); }

}

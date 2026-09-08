package com.github.laxika.magicalvibes.model.effect;

/** Resolves Specter's Shriek's hand reveal, optional exile, and conditional hand exile. */
public record SpectersShriekEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}

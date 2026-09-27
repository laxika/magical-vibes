package com.github.laxika.magicalvibes.model.effect;

/** Makes each permanent in the bound target group connive in order. */
public record ConniveEachTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

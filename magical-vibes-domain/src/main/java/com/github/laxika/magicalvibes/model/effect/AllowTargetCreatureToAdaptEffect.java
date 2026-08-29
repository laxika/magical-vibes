package com.github.laxika.magicalvibes.model.effect;

/** Gives the target creature one temporary permission to Adapt despite its +1/+1 counters. */
public record AllowTargetCreatureToAdaptEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

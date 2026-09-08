package com.github.laxika.magicalvibes.model.effect;

/** Applies the suspected designation to a target creature. */
public record SuspectTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

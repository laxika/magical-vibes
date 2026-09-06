package com.github.laxika.magicalvibes.model.effect;

/** Moves every concrete counter from the implicit source permanent onto target creature. */
public record MoveAllCountersFromSourceToTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

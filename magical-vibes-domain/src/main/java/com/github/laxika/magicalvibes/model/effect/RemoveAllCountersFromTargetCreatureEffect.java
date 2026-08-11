package com.github.laxika.magicalvibes.model.effect;

/** Removes every counter of every kind from target creature. */
public record RemoveAllCountersFromTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

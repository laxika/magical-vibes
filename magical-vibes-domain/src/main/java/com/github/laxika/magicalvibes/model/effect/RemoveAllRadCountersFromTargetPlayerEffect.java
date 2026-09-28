package com.github.laxika.magicalvibes.model.effect;

/** Removes all rad counters from target player. */
public record RemoveAllRadCountersFromTargetPlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}

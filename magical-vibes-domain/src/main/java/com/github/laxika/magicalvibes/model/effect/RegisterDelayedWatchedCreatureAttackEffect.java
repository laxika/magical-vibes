package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Registers a delayed trigger watching the chosen creature attack an opponent this turn. */
public record RegisterDelayedWatchedCreatureAttackEffect(List<CardEffect> effects)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

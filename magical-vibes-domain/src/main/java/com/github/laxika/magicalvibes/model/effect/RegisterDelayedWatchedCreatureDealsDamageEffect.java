package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Registers a delayed trigger that watches the target creature for any damage it deals until end
 * of turn. The resulting ability is controlled by the resolving spell's controller.
 */
public record RegisterDelayedWatchedCreatureDealsDamageEffect(List<CardEffect> effects)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

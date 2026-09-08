package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Applies one {@link BoostTargetCreatureEffect} to each target in a multi-target group, pairing
 * the boosts with the targets by their chosen position.
 */
public record BoostTargetCreaturesByPositionEffect(List<BoostTargetCreatureEffect> boosts)
        implements CardEffect {

    public BoostTargetCreaturesByPositionEffect {
        boosts = List.copyOf(boosts);
        if (boosts.isEmpty()) {
            throw new IllegalArgumentException("At least one positional boost is required");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

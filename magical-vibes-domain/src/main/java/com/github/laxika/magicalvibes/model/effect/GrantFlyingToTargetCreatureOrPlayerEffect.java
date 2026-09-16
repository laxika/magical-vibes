package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants flying until end of turn to a target creature or player (Sarah's Wings).
 */
public record GrantFlyingToTargetCreatureOrPlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyTarget());
    }
}

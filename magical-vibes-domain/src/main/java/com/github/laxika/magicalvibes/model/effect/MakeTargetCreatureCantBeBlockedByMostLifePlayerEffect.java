package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses a player tied for most life and prevents creatures that player controls from blocking
 * the target creature until end of turn.
 */
public record MakeTargetCreatureCantBeBlockedByMostLifePlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

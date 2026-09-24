package com.github.laxika.magicalvibes.model.effect;

/** Replaces the next destruction of target land this turn by removing all damage marked on it. */
public record RemoveDamageFromTargetLandInsteadOfNextDestructionEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.land());
    }
}

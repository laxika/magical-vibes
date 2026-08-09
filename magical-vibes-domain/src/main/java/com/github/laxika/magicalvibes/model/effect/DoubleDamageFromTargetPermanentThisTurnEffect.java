package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: for the rest of the turn, the targeted permanent deals double damage instead.
 * Multiple instances stack multiplicatively and the grant is keyed to that permanent's battlefield
 * identity.
 */
public record DoubleDamageFromTargetPermanentThisTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}

package com.github.laxika.magicalvibes.model.effect;

/**
 * Target creature you control deals damage equal to its power to each other creature (on any
 * battlefield, including creatures its controller controls) and to each opponent of the spell's
 * controller. The targeted creature is the source of all that damage (CR 608.2h), so protection
 * from it, lifelink and "deals damage" triggers key off the creature rather than off the spell.
 * Used by Chandra's Ignition.
 */
public record TargetCreatureDealsPowerDamageToEachOtherCreatureAndEachOpponentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

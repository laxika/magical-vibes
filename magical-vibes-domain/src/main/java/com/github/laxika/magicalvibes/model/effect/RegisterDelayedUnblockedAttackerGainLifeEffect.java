package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger for the chosen creature: this turn, whenever it attacks and isn't
 * blocked, its controller may gain life equal to its power. If they do, it assigns no combat
 * damage this turn.
 */
public record RegisterDelayedUnblockedAttackerGainLifeEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

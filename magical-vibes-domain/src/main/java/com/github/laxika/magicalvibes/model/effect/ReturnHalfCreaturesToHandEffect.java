package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns half of the target player's creatures to their owners' hands, rounded up. The resolving
 * spell's controller chooses the creatures at resolution.
 */
public record ReturnHalfCreaturesToHandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}

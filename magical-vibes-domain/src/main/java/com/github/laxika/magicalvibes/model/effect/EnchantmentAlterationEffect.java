package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToCreatureOrLandPredicate;

/**
 * Moves the targeted Aura to a different permanent of the same type as its current host. The
 * destination is chosen during resolution rather than targeted.
 */
public record EnchantmentAlterationEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(),
                new PermanentIsAuraAttachedToCreatureOrLandPredicate());
    }
}

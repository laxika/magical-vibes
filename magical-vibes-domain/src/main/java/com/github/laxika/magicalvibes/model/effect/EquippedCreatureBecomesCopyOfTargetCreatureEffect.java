package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Makes the creature attached to the source Equipment become a copy of the target creature
 * while that Equipment remains attached to it.
 */
public record EquippedCreatureBecomesCopyOfTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(
                TargetPredicates.creature(),
                new PermanentNotPredicate(new PermanentIsHostOfSourceAuraPredicate()));
    }
}

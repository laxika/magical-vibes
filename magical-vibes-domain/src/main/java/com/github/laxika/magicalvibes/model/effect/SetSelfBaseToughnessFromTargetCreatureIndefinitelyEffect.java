package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Sets the source permanent's base toughness indefinitely to one plus the target creature's
 * current power. The target's power is read when this effect resolves.
 *
 * @param targetPredicate narrows the legal target, such as a creature blocking or blocked by the source
 */
public record SetSelfBaseToughnessFromTargetCreatureIndefinitelyEffect(
        PermanentPredicate targetPredicate
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), targetPredicate);
    }
}

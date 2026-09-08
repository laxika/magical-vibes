package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Sets the source permanent's base toughness to the target creature's current power plus an
 * offset. The resulting base-toughness setting lasts indefinitely.
 *
 * @param toughnessOffset the amount added to the target creature's power
 * @param targetPredicate the additional restriction on the target creature
 */
public record SetSelfBaseToughnessFromTargetPowerEffect(int toughnessOffset,
                                                         PermanentPredicate targetPredicate)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), targetPredicate);
    }
}

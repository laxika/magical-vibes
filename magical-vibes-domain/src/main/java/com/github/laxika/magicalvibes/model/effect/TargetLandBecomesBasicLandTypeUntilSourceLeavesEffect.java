package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Changes a target land to a fixed basic land type for as long as the source permanent remains on
 * the battlefield. The source's static companion effect applies the change in the layered
 * characteristic state, so the land reverts automatically when the source leaves.
 */
public record TargetLandBecomesBasicLandTypeUntilSourceLeavesEffect(CardSubtype subtype)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.land());
    }
}

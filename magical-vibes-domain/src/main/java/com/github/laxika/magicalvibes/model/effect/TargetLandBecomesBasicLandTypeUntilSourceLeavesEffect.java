package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Changes a target land to a fixed basic land type for as long as the source permanent remains on
 * the battlefield. Resolution creates a {@code WHILE_SOURCE_REMAINS} floating layer-4 effect with
 * its own timestamp (CR 613.7), so it overrides type changes that existed when it resolved and
 * ends when the source leaves.
 */
public record TargetLandBecomesBasicLandTypeUntilSourceLeavesEffect(CardSubtype subtype)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.land());
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Changes a target land to a fixed basic land type until the source permanent leaves the battlefield (Gaea's Liege,
 * Graceful Antelope: {@link Duration#UNTIL_SOURCE_LEAVES}), or for as long as it remains on the battlefield (Tide
 * Shaper: {@link Duration#WHILE_SOURCE_REMAINS}, which also ends as the source phases out, CR 702.26f). Resolution
 * creates a floating layer-4 effect with that duration and its own timestamp (CR 613.7), so it overrides type changes
 * that existed when it resolved and ends when the source leaves.
 */
public record TargetLandBecomesBasicLandTypeUntilSourceLeavesEffect(CardSubtype subtype, Duration duration)
        implements CardEffect {

    public TargetLandBecomesBasicLandTypeUntilSourceLeavesEffect(CardSubtype subtype) {
        this(subtype, Duration.UNTIL_SOURCE_LEAVES);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.land());
    }
}

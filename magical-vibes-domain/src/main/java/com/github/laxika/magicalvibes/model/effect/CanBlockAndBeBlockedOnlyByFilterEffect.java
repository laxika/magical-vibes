package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static restriction for creatures that can block only matching creatures and can be blocked only
 * by matching creatures.
 */
public record CanBlockAndBeBlockedOnlyByFilterEffect(PermanentPredicate matchingPredicate,
                                                       String matchingDescription)
        implements BlockabilityRestrictionEffect, BlockingRestrictionEffect {

    @Override
    public PermanentPredicate blockableOnlyBy() {
        return matchingPredicate;
    }

    @Override
    public String blockableOnlyByDescription() {
        return matchingDescription;
    }

    @Override
    public PermanentPredicate canBlockOnlyAttackersMatching() {
        return matchingPredicate;
    }

    @Override
    public String canBlockOnlyAttackersDescription() {
        return matchingDescription;
    }
}

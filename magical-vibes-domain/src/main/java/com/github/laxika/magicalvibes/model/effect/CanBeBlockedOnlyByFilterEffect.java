package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static evasion restriction on attackers.
 * This creature can be blocked only by blockers that match the provided blocker filter.
 */
public record CanBeBlockedOnlyByFilterEffect(PermanentPredicate blockerPredicate, String allowedBlockersDescription)
        implements CardEffect {
}

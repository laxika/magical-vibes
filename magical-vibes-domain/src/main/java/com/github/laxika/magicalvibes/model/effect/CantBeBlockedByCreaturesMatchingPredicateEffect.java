package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static evasion restriction on attackers.
 * This creature can't be blocked by creatures that match the provided blocker filter
 * (e.g. Taoist Mystic: "can't be blocked by creatures with horsemanship").
 */
public record CantBeBlockedByCreaturesMatchingPredicateEffect(PermanentPredicate blockerPredicate)
        implements BlockabilityRestrictionEffect {

    @Override
    public PermanentPredicate cantBeBlockedByCreaturesMatching() {
        return blockerPredicate;
    }
}

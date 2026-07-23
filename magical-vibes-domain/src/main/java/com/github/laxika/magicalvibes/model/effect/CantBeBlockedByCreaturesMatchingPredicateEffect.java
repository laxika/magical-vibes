package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static evasion restriction on attackers.
 * This creature can't be blocked by creatures that match the provided blocker filter
 * (e.g. Taoist Mystic: "can't be blocked by creatures with horsemanship").
 * When {@code onlyIfDefenderControls} is non-null, the restriction only applies while the
 * defending player controls a matching permanent (Arctic Foxes).
 */
public record CantBeBlockedByCreaturesMatchingPredicateEffect(
        PermanentPredicate blockerPredicate,
        PermanentPredicate onlyIfDefenderControls)
        implements BlockabilityRestrictionEffect {

    public CantBeBlockedByCreaturesMatchingPredicateEffect(PermanentPredicate blockerPredicate) {
        this(blockerPredicate, null);
    }

    @Override
    public PermanentPredicate cantBeBlockedByCreaturesMatching() {
        return blockerPredicate;
    }

    @Override
    public PermanentPredicate cantBeBlockedByCreaturesMatchingOnlyIfDefenderControls() {
        return onlyIfDefenderControls;
    }
}

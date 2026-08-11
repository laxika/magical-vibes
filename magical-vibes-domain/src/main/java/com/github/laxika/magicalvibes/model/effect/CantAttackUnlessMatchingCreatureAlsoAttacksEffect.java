package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static attack restriction: this creature can't attack unless another declared attacker matches
 * the given permanent predicate.
 */
public record CantAttackUnlessMatchingCreatureAlsoAttacksEffect(PermanentPredicate predicate,
                                                                String requirementDescription)
        implements MatchingAttackerRestrictionEffect {

    @Override
    public PermanentPredicate matchingAttackerPredicate() {
        return predicate;
    }

    @Override
    public String restrictionDescription() {
        return requirementDescription;
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each target player draws cards equal to the number of permanents matching {@code filter} attached to "that player",
 * the stack entry's triggering player (the enchanted player whose upkeep it is, Curse of Surveillance), counted as the
 * ability resolves.
 */
public record EachTargetPlayerDrawsCardsEqualToAttachedCountEffect(PermanentPredicate filter)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}

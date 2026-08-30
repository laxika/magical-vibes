package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * Each chosen player draws cards equal to the number of permanents attached to the enchanted player
 * that match the filter. The enchanted player is captured when the triggered ability is created.
 */
public record EachTargetPlayerDrawsCardsEqualToAttachedCountEffect(PermanentPredicate filter, UUID enchantedPlayerId)
        implements CardEffect {

    public EachTargetPlayerDrawsCardsEqualToAttachedCountEffect(PermanentPredicate filter) {
        this(filter, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}

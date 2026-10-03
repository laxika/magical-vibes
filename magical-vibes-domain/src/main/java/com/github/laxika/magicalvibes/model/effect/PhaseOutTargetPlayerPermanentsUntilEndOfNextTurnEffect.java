package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Phases out matching permanents controlled by a target player until the end of your next turn. */
public record PhaseOutTargetPlayerPermanentsUntilEndOfNextTurnEffect(PermanentPredicate filter)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}

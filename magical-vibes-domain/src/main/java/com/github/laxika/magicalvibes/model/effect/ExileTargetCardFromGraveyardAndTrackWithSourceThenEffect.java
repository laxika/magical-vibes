package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Exiles a targeted graveyard card with the source permanent, then resolves a follow-up effect. */
public record ExileTargetCardFromGraveyardAndTrackWithSourceThenEffect(
        CardPredicate filter,
        GraveyardSearchScope scope,
        CardEffect thenEffect
) implements CardEffect, OptionalTargetEffect {

    public ExileTargetCardFromGraveyardAndTrackWithSourceThenEffect {
        if (scope == null) {
            throw new IllegalArgumentException("ExileTargetCardFromGraveyardAndTrackWithSourceThenEffect requires a scope");
        }
        if (thenEffect == null) {
            throw new IllegalArgumentException("ExileTargetCardFromGraveyardAndTrackWithSourceThenEffect requires a follow-up effect");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(filter == null
                ? TargetPredicates.graveyardCard(scope)
                : TargetPredicates.graveyardCards(filter, scope));
    }

    @Override
    public boolean hasOptionalTarget() {
        return true;
    }
}

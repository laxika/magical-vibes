package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Exiles a targeted card from any graveyard, then resolves a follow-up effect if it succeeds. */
public record ExileTargetCardFromGraveyardThenEffect(
        CardPredicate filter,
        CardEffect thenEffect
) implements CardEffect {

    public ExileTargetCardFromGraveyardThenEffect {
        if (thenEffect == null) {
            throw new IllegalArgumentException("ExileTargetCardFromGraveyardThenEffect requires a follow-up effect");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(filter == null
                ? TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS)
                : TargetPredicates.graveyardCards(filter, GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Exiles a targeted graveyard card, creates a copy of it, and offers the copy for a free cast. */
public record ExileTargetCardFromGraveyardAndMayCastCopyEffect(
        CardPredicate filter,
        GraveyardSearchScope scope,
        int lifeLossOnCast
) implements CardEffect {

    public ExileTargetCardFromGraveyardAndMayCastCopyEffect(
            CardPredicate filter, GraveyardSearchScope scope) {
        this(filter, scope, 0);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(filter, scope));
    }
}

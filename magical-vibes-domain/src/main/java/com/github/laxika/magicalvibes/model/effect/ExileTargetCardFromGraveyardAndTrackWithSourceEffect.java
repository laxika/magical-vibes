package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Exiles a targeted graveyard card and tracks it as exiled with the source permanent. */
public record ExileTargetCardFromGraveyardAndTrackWithSourceEffect(
        CardPredicate filter,
        GraveyardSearchScope scope
) implements CardEffect {

    public ExileTargetCardFromGraveyardAndTrackWithSourceEffect(GraveyardSearchScope scope) {
        this(null, scope);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(filter == null
                ? TargetPredicates.graveyardCard(scope)
                : TargetPredicates.graveyardCards(filter, scope));
    }
}

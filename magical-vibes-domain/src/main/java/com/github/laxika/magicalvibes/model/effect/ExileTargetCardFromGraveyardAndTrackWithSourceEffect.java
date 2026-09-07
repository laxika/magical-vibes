package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

/** Exiles a targeted graveyard card and tracks it as exiled with the source permanent. */
public record ExileTargetCardFromGraveyardAndTrackWithSourceEffect(GraveyardSearchScope scope)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(scope));
    }
}

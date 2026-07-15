package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

public record CastTargetInstantOrSorceryFromGraveyardEffect(
        GraveyardSearchScope scope,
        boolean withoutPayingManaCost
) implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.GRAVEYARD_CARD); }
}

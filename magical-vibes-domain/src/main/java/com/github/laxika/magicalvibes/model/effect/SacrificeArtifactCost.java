package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record SacrificeArtifactCost() implements CostEffect {

    private static final PermanentPredicate ARTIFACT_FILTER = new PermanentIsArtifactPredicate();

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return ARTIFACT_FILTER;
    }
}

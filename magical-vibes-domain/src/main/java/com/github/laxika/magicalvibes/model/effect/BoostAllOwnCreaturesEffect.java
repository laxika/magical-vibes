package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record BoostAllOwnCreaturesEffect(
        int powerBoost,
        int toughnessBoost,
        PermanentPredicate filter
) implements CardEffect {

    public BoostAllOwnCreaturesEffect(int powerBoost, int toughnessBoost) {
        this(powerBoost, toughnessBoost, null);
    }
}

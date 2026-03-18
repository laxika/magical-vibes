package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record BoostSelfPerOpponentPermanentEffect(
        int powerPerPermanent,
        int toughnessPerPermanent,
        PermanentPredicate filter
) implements CardEffect {
}

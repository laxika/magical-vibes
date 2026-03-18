package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record DestroyAllPermanentsAndGainLifePerDestroyedEffect(
        PermanentPredicate filter,
        int lifePerDestroyed
) implements CardEffect {
}

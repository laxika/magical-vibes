package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record DestroyAllPermanentsEffect(
        PermanentPredicate filter,
        boolean cannotBeRegenerated
) implements CardEffect {

    public DestroyAllPermanentsEffect(PermanentPredicate filter) {
        this(filter, false);
    }
}

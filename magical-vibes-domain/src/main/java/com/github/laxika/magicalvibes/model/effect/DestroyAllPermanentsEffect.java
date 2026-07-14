package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record DestroyAllPermanentsEffect(
        PermanentPredicate filter,
        boolean cannotBeRegenerated
) implements BoardWipeEffect {

    public DestroyAllPermanentsEffect(PermanentPredicate filter) {
        this(filter, false);
    }

    /** Destroy-all always sweeps the board. */
    @Override
    public boolean sweepsBoard() {
        return true;
    }
}

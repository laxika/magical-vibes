package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller chooses a color at resolution, then destroys every matching permanent of that
 * color across all battlefields.
 */
public record DestroyAllPermanentsOfChosenColorEffect(PermanentPredicate filter)
        implements BoardWipeEffect {

    public DestroyAllPermanentsOfChosenColorEffect() {
        this(null);
    }

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}

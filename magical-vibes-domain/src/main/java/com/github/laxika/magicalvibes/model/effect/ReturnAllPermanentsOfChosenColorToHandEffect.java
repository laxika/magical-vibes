package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller chooses a color at resolution, then returns every matching permanent of that
 * color to its owner's hand.
 */
public record ReturnAllPermanentsOfChosenColorToHandEffect(PermanentPredicate filter) implements BoardWipeEffect {

    public ReturnAllPermanentsOfChosenColorToHandEffect() {
        this(null);
    }

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}

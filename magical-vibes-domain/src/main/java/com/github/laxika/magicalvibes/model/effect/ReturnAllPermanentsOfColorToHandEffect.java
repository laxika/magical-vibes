package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Returns every permanent of the specified color matching the optional filter to its owner's
 * hand.
 */
public record ReturnAllPermanentsOfColorToHandEffect(CardColor color, PermanentPredicate filter,
                                                     boolean opponentsOnly)
        implements BoardWipeEffect {

    public ReturnAllPermanentsOfColorToHandEffect(CardColor color) {
        this(color, null, false);
    }

    public ReturnAllPermanentsOfColorToHandEffect(CardColor color, PermanentPredicate filter) {
        this(color, filter, false);
    }

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}

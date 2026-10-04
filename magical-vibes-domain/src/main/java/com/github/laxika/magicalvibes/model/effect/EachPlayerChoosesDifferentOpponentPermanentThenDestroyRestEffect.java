package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each player chooses a different matching permanent controlled by another player, then all
 * other matching permanents are destroyed.
 */
public record EachPlayerChoosesDifferentOpponentPermanentThenDestroyRestEffect(
        PermanentPredicate filter) implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}

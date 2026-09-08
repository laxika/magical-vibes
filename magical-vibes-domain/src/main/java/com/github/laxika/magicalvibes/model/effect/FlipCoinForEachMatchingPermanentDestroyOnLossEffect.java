package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Flips a coin for each permanent matching {@code filter}, then destroys each one whose flip is
 * lost.
 *
 * <p>The matching permanents are captured before the first destruction, and all lost-flip
 * permanents are destroyed as one batch.
 *
 * @param filter which permanents receive a coin flip
 */
public record FlipCoinForEachMatchingPermanentDestroyOnLossEffect(PermanentPredicate filter)
        implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}

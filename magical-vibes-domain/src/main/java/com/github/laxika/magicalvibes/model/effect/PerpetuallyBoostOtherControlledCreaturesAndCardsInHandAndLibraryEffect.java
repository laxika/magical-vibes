package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Perpetually boosts matching creatures the controller controls, excluding the source, and
 * matching cards in the controller's hand and library.
 */
public record PerpetuallyBoostOtherControlledCreaturesAndCardsInHandAndLibraryEffect(
        CardPredicate filter, int powerBoost, int toughnessBoost) implements CardEffect {
}

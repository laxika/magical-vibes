package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Seeks one matching card into hand and perpetually reduces that card's generic cast cost by one. */
public record SeekLibraryAndPerpetuallyReduceSoughtCardEffect(CardPredicate filter)
        implements CardEffect {
}

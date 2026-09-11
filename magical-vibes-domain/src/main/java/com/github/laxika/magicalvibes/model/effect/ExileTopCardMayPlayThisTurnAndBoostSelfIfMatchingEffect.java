package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles the top card of the controller's library and lets them play it until end of turn.
 * If the exiled card matches {@code matchFilter}, the source permanent gets the configured
 * power and toughness boost until end of turn.
 */
public record ExileTopCardMayPlayThisTurnAndBoostSelfIfMatchingEffect(
        CardPredicate matchFilter,
        int power,
        int toughness
) implements CardEffect {
}

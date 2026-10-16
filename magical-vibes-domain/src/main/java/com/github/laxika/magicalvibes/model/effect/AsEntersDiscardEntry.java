package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.BattlefieldEntryRequest;

/**
 * Carries the parked battlefield entry through the choices of
 * {@link DiscardCreatureCardsAsEntersForKeywordCountersEffect}: the number-of-cards prompt and the
 * discards that follow it. Never part of a card; created while the permanent is entering.
 */
public record AsEntersDiscardEntry(BattlefieldEntryRequest entryRequest,
                                   DiscardCreatureCardsAsEntersForKeywordCountersEffect effect)
        implements CardEffect {
}

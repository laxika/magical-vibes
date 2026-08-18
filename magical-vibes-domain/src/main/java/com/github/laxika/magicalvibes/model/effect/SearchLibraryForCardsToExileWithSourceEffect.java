package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * ON_ENTER_BATTLEFIELD: the controller searches their library for up to {@code maxCount} cards
 * matching {@code filter}, exiles each one tracked "with" the source permanent (via
 * {@code GameData.exiledCards} / {@code sourcePermanentId}), then shuffles.
 *
 * <p>The controller may stop the repeated single-card pick at any time. A non-positive
 * {@code maxCount} is treated as no cards.
 */
public record SearchLibraryForCardsToExileWithSourceEffect(CardPredicate filter, int maxCount)
        implements CardEffect {

    public SearchLibraryForCardsToExileWithSourceEffect(CardPredicate filter) {
        this(filter, Integer.MAX_VALUE);
    }
}

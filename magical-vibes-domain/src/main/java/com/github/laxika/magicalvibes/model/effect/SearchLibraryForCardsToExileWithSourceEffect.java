package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * ON_ENTER_BATTLEFIELD: the controller searches their library for any number of cards matching
 * {@code filter}, exiles each one tracked "with" the source permanent (via
 * {@code GameData.exiledCards} / {@code sourcePermanentId}), then shuffles.
 *
 * <p>"Any number" is modelled as a repeated single-card pick that the controller may stop at any
 * time (fail-to-find). Pair with {@link PutCardExiledWithSourceIntoHandEffect} on an upkeep trigger
 * to retrieve the exiled cards one at a time. Used by Endless Horizons (Plains).
 */
public record SearchLibraryForCardsToExileWithSourceEffect(CardPredicate filter) implements CardEffect {
}

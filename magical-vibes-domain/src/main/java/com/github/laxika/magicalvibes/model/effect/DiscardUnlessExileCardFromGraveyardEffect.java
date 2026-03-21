package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "Discard a card unless you exile a [predicate] card from your graveyard."
 * <p>
 * The controller must discard a card unless they choose to exile a card
 * matching the given predicate from their graveyard instead.
 * If no matching cards exist in the graveyard, the discard is mandatory.
 *
 * @param predicate filter for which graveyard cards can be exiled to avoid discarding
 *                  (e.g. {@code CardIsHistoricPredicate} for historic cards)
 */
public record DiscardUnlessExileCardFromGraveyardEffect(CardPredicate predicate) implements CardEffect {
}

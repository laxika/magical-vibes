package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Controller discards {@code amount} cards, then returns a card from their graveyard to their hand
 * for each card discarded this way. The number returned equals the number actually discarded
 * ({@code min(amount, hand size)}), so an oversized {@code amount} (e.g. Recall's X) only returns as
 * many cards as were discarded. Both the discard and the graveyard returns are chosen by the
 * controller; the returns run once the (interactive) discard completes. Recall.
 *
 * @param amount number of cards to discard (and, in turn, the maximum number returned)
 */
public record DiscardThenReturnFromGraveyardToHandEffect(DynamicAmount amount) implements CardEffect {
}

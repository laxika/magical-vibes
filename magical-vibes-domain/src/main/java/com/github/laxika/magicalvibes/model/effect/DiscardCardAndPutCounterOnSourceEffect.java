package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Discards one matching card, then puts a +1/+1 counter on the source permanent.
 *
 * @param cardFilter filter for the card that may be discarded; {@code null} accepts any card
 * @param cardDescription text used in the discard prompt and game log
 */
public record DiscardCardAndPutCounterOnSourceEffect(CardPredicate cardFilter, String cardDescription)
        implements CardEffect {
}

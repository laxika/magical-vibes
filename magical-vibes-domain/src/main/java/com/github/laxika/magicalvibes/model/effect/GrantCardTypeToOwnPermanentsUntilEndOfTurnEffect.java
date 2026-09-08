package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * On resolution, additively grants a card type until end of turn to matching permanents the
 * controller controls.
 */
public record GrantCardTypeToOwnPermanentsUntilEndOfTurnEffect(CardType cardType,
                                                                PermanentPredicate filter)
        implements CardEffect {

    public GrantCardTypeToOwnPermanentsUntilEndOfTurnEffect(CardType cardType) {
        this(cardType, null);
    }
}

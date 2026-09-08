package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Adds a card type until end of turn to matching permanents controlled by the effect's controller. */
public record AddCardTypeToOwnPermanentsUntilEndOfTurnEffect(CardType cardType, PermanentPredicate filter)
        implements CardEffect {
}

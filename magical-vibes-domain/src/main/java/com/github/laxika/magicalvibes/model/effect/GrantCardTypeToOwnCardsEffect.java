package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Static effect that grants a card type to matching cards the controller owns in every zone. */
public record GrantCardTypeToOwnCardsEffect(CardType cardType, CardPredicate filter)
        implements OwnCardTypeGrantingEffect {
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/** Static layer-4 effect that removes a card type from the permanent attached to the source. */
public record RemoveCardTypeFromAttachedPermanentEffect(CardType cardType, GrantScope scope)
        implements CardEffect {

    public RemoveCardTypeFromAttachedPermanentEffect(CardType cardType) {
        this(cardType, GrantScope.EQUIPPED_CREATURE);
    }
}

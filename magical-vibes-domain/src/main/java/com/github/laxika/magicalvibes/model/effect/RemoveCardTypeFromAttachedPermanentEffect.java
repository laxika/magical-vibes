package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/** Removes a card type in the stated scope, optionally only while its source is attached. */
public record RemoveCardTypeFromAttachedPermanentEffect(CardType cardType, GrantScope scope, EffectDuration duration)
        implements CardEffect {

    public RemoveCardTypeFromAttachedPermanentEffect(CardType cardType, GrantScope scope) {
        this(cardType, scope, EffectDuration.PERMANENT);
    }

    public RemoveCardTypeFromAttachedPermanentEffect(CardType cardType) {
        this(cardType, GrantScope.EQUIPPED_CREATURE);
    }
}

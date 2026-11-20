package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/** Removes a card type in the stated subject, optionally only while its source is attached. */
public record RemoveCardTypeFromAttachedPermanentEffect(CardType cardType, PermanentSubject subject, Duration duration)
        implements CardEffect {

    public RemoveCardTypeFromAttachedPermanentEffect(CardType cardType, PermanentSubject subject) {
        this(cardType, subject, Duration.PERMANENT);
    }

    public RemoveCardTypeFromAttachedPermanentEffect(CardType cardType) {
        this(cardType, Subjects.attachedCreature());
    }
}

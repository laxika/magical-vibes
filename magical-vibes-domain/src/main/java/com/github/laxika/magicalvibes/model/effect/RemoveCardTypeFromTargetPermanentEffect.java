package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Makes the target or source permanent lose the given card type until end of turn.
 *
 * @param cardType the card type to remove
 * @param subject whether to affect the target or the source permanent
 */
public record RemoveCardTypeFromTargetPermanentEffect(CardType cardType, PermanentSubject subject)
        implements CardEffect {

    public RemoveCardTypeFromTargetPermanentEffect(CardType cardType) {
        this(cardType, Subjects.target());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Source
                ? TargetSpec.NONE
                : TargetSpec.benign(TargetPredicates.permanent());
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Makes the target or source permanent lose the given card type until end of turn.
 *
 * @param cardType the card type to remove
 * @param scope whether to affect the target or the source permanent
 */
public record RemoveCardTypeFromTargetPermanentEffect(CardType cardType, GrantScope scope) implements CardEffect {

    public RemoveCardTypeFromTargetPermanentEffect(CardType cardType) {
        this(cardType, GrantScope.TARGET);
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.SELF ? TargetSpec.NONE : TargetSpec.benign(TargetPredicates.permanent());
    }
}

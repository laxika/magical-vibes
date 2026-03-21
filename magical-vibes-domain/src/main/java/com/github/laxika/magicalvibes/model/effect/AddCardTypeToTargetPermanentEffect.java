package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Adds a card type to the target permanent in addition to its other types.
 *
 * @param cardType   the card type to add
 * @param persistent when {@code false}, the type is added to {@code grantedCardTypes} (cleared each turn);
 *                   when {@code true}, it is added to {@code persistentGrantedCardTypes} (permanent, survives turn resets).
 */
public record AddCardTypeToTargetPermanentEffect(CardType cardType, boolean persistent) implements CardEffect {

    /** Backward-compatible constructor — non-persistent (until end of turn). */
    public AddCardTypeToTargetPermanentEffect(CardType cardType) {
        this(cardType, false);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}

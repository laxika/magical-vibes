package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Makes the target permanent lose the given card type until end of turn.
 *
 * @param cardType the card type to remove
 */
public record RemoveCardTypeFromTargetPermanentEffect(CardType cardType) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}

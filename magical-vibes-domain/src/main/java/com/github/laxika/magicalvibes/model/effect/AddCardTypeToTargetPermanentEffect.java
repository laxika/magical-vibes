package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

public record AddCardTypeToTargetPermanentEffect(CardType cardType) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}

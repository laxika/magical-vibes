package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

public record GrantColorUntilEndOfTurnEffect(CardColor color) implements CardEffect {

    public boolean canTargetPermanent() {
        return true;
    }
}

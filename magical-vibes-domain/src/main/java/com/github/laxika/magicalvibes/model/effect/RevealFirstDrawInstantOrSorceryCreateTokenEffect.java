package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

/** Offers to reveal the first card drawn each turn and creates a token for an instant or sorcery. */
public record RevealFirstDrawInstantOrSorceryCreateTokenEffect(CreateTokenEffect tokenEffect)
        implements FirstDrawRevealTriggerEffect {

    @Override
    public CardEffect effectFor(Card drawnCard) {
        return drawnCard.hasType(CardType.INSTANT) || drawnCard.hasType(CardType.SORCERY)
                ? new MayEffect(tokenEffect, "Reveal " + drawnCard.getName() + "?")
                : null;
    }

    @Override
    public boolean revealBeforeChoice() {
        return false;
    }
}

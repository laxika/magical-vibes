package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Offers to reveal the first card drawn each turn as it is drawn; revealing an instant or sorcery creates a token. */
public record RevealFirstDrawInstantOrSorceryCreateTokenEffect(CreateTokenEffect tokenEffect)
        implements FirstDrawRevealTriggerEffect {

    @Override
    public CardEffect effectFor(Card drawnCard) {
        return new RevealDrawnCardCreateTokenIfInstantOrSorceryEffect(drawnCard, tokenEffect);
    }

    @Override
    public boolean revealBeforeChoice() {
        return false;
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Offers to reveal the first card drawn each turn as it is drawn, on any player's turn. Revealing an
 * instant or sorcery triggers a copy of it that may be cast; revealing any other card has no further
 * effect.
 */
public record RevealFirstDrawInstantOrSorceryEffect() implements FirstDrawRevealTriggerEffect {

    @Override
    public CardEffect effectFor(Card drawnCard) {
        return new RevealDrawnCardIfInstantOrSorceryEffect(drawnCard,
                new CopyDrawnInstantOrSorceryAndMayCastCopyEffect(drawnCard));
    }

    @Override
    public boolean onlyOnControllerTurn() {
        return false;
    }

    @Override
    public boolean revealBeforeChoice() {
        return false;
    }
}

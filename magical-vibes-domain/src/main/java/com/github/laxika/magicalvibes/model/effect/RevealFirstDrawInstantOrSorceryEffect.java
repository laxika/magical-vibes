package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Offers to reveal the first card drawn each turn, on any player's turn. Revealing an instant or
 * sorcery copies it; revealing any other card has no further effect.
 */
public record RevealFirstDrawInstantOrSorceryEffect() implements FirstDrawRevealTriggerEffect {

    @Override
    public CardEffect effectFor(Card drawnCard) {
        return new MayEffect(
                new CopyDrawnInstantOrSorceryAndMayCastCopyEffect(drawnCard),
                "Reveal " + drawnCard.getName() + "?");
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

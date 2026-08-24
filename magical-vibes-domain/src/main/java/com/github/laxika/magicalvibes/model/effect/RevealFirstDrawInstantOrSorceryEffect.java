package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

/** Offers to reveal the first card drawn each turn and copies it when it is an instant or sorcery. */
public record RevealFirstDrawInstantOrSorceryEffect() implements FirstDrawRevealTriggerEffect {

    @Override
    public CardEffect effectFor(Card drawnCard) {
        return drawnCard.hasType(CardType.INSTANT) || drawnCard.hasType(CardType.SORCERY)
                ? new MayEffect(
                        new CopyDrawnInstantOrSorceryAndMayCastCopyEffect(drawnCard),
                        "Reveal " + drawnCard.getName() + "?")
                : null;
    }

    @Override
    public boolean onlyOnControllerTurn() {
        return true;
    }

    @Override
    public boolean revealBeforeChoice() {
        return false;
    }
}

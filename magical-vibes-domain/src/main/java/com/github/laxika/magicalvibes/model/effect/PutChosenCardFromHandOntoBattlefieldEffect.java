package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Puts the card selected by a preceding hand choice onto the battlefield. */
public record PutChosenCardFromHandOntoBattlefieldEffect(UUID cardId, boolean enterTapped)
        implements CardEffect, ChosenCardAwareEffect {

    public PutChosenCardFromHandOntoBattlefieldEffect() {
        this(null, false);
    }

    public PutChosenCardFromHandOntoBattlefieldEffect(boolean enterTapped) {
        this(null, enterTapped);
    }

    @Override
    public CardEffect withChosenCard(Card card) {
        return new PutChosenCardFromHandOntoBattlefieldEffect(card.getId(), enterTapped);
    }
}

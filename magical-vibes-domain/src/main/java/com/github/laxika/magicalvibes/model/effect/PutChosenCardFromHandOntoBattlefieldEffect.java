package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Puts the card selected by a preceding hand choice onto the battlefield. */
public record PutChosenCardFromHandOntoBattlefieldEffect(UUID cardId)
        implements CardEffect, ChosenCardAwareEffect {

    public PutChosenCardFromHandOntoBattlefieldEffect() {
        this(null);
    }

    @Override
    public CardEffect withChosenCard(Card card) {
        return new PutChosenCardFromHandOntoBattlefieldEffect(card.getId());
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Chooses a creature card in the controller's hand and conjures a duplicate into that hand. */
public record ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffect(Card chosenCard)
        implements CardEffect, ChosenCardAwareEffect {

    public ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffect() {
        this(null);
    }

    @Override
    public CardEffect withChosenCard(Card card) {
        return new ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffect(card);
    }
}

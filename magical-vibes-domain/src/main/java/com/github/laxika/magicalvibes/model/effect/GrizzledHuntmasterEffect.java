package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Resolves Grizzled Huntmaster's optional hand exile, same-name exile, and conjure sequence. */
public record GrizzledHuntmasterEffect(
        String cardName,
        int handExiledCount,
        boolean sameNameCardsExiled,
        Card conjureSourceCard) implements CardEffect, ChosenCardAwareEffect {

    public GrizzledHuntmasterEffect() {
        this(null, 0, false, null);
    }

    @Override
    public CardEffect withChosenCard(Card card) {
        if (cardName == null) {
            return new GrizzledHuntmasterEffect(card.getName(), 1, false, null);
        }
        return new GrizzledHuntmasterEffect(cardName, handExiledCount, sameNameCardsExiled, card);
    }
}

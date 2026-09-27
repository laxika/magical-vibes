package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Optionally exiles an instant or sorcery from hand, then exiles any number of its same-named
 * cards from hand and library and seeks one matching card for each copy exiled from hand.
 */
public record ExileSameNameCardsFromHandAndLibraryThenSeekEffect(
        String cardName, CardPredicate seekFilter) implements CardEffect, ChosenCardAwareEffect {

    public ExileSameNameCardsFromHandAndLibraryThenSeekEffect() {
        this(null, new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY))));
    }

    @Override
    public CardEffect withChosenCard(Card card) {
        return new ExileSameNameCardsFromHandAndLibraryThenSeekEffect(card.getName(), seekFilter);
    }
}

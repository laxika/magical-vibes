package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Optionally exiles an instant or sorcery from hand, then exiles any number of its same-named
 * cards from hand and library and seeks one matching card for each copy exiled from hand.
 *
 * <p>The effect first asks for the card, with {@link #afterChoice()} as the choice's follow-up; that step reads the
 * exiled card from the stack entry ({@code StackEntry.getChosenObjectCard()}).
 *
 * @param seekFilter the cards sought
 * @param cardChosen whether the card is already chosen (the step after the choice)
 */
public record ExileSameNameCardsFromHandAndLibraryThenSeekEffect(
        CardPredicate seekFilter, boolean cardChosen) implements CardEffect {

    public ExileSameNameCardsFromHandAndLibraryThenSeekEffect() {
        this(new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY))), false);
    }

    /** The step after the card is chosen: it reads the stack entry's chosen card. */
    public ExileSameNameCardsFromHandAndLibraryThenSeekEffect afterChoice() {
        return new ExileSameNameCardsFromHandAndLibraryThenSeekEffect(seekFilter, true);
    }
}

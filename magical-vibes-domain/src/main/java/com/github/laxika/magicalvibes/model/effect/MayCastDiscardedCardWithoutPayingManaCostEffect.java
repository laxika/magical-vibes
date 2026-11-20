package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * "You may cast it without paying its mana cost", as the ability resolves: each card {@link #cards()} names that
 * matches {@link #filter()} and that the controller owns is offered for a free cast where it is (its graveyard or
 * exile). Malcolm, Alluring Scoundrel's "you may cast the discarded card" is {@code Subjects.cardsThisWay(DISCARD)};
 * Hidetsugu and Kairi's "if it's an instant or sorcery card, you may cast it" after its exile step is
 * {@code Subjects.cardsThisWay(EXILE)} with an instant-or-sorcery filter. A declined card stays where it is.
 *
 * @param cards  the cards offered
 * @param filter which of them may be cast; {@code null} for any nonland card (a land card can't be cast)
 */
public record MayCastDiscardedCardWithoutPayingManaCostEffect(CardSubject cards, CardPredicate filter)
        implements CardEffect {

    public MayCastDiscardedCardWithoutPayingManaCostEffect {
        Objects.requireNonNull(cards, "cards");
    }

    /** "You may cast [those cards] without paying their mana cost": any nonland card {@code cards} names. */
    public MayCastDiscardedCardWithoutPayingManaCostEffect(CardSubject cards) {
        this(cards, null);
    }
}

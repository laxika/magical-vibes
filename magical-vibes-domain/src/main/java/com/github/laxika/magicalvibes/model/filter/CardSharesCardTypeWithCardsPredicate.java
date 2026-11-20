package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.effect.CardSubject;

import java.util.Objects;

/**
 * Matches a card that shares a card type with one of the cards {@code cards} names: "seek two cards that don't share a
 * card type with the discarded card" is its negation over {@code Subjects.cardsThisWay(StepKind.DISCARD)} (Anguished
 * Recollection). The subject needs the resolving entry, so a handler binds the predicate to the card types those cards
 * have as the effect applies ({@code SubjectResolutionService.bindCardSubjects}); unbound it matches nothing.
 */
public record CardSharesCardTypeWithCardsPredicate(CardSubject cards) implements CardPredicate {

    public CardSharesCardTypeWithCardsPredicate {
        Objects.requireNonNull(cards, "cards");
    }
}

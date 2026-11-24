package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.effect.CardSubject;

import java.util.Objects;

/**
 * Matches a card with the same name as one of the cards {@code cards} names: "search your library for a card with the
 * same name as that card" after a reveal from the hand, over {@code Subjects.revealedThisWay()} (Infernal Tutor,
 * Assembly Hall). A card with no name shares it with nothing (CR 201.2a). The subject needs the resolving entry, so a
 * handler binds the predicate to those cards' names as the effect applies
 * ({@code SubjectResolutionService.bindCardSubjects}); unbound, or with no card named, it matches nothing.
 */
public record CardSharesNameWithCardsPredicate(CardSubject cards) implements CardPredicate {

    public CardSharesNameWithCardsPredicate {
        Objects.requireNonNull(cards, "cards");
    }
}

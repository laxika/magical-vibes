package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.effect.PermanentSubject;

import java.util.Objects;

/**
 * Matches a card that shares a card type with one of the permanents {@code permanents} names: "a permanent card that
 * shares a card type with the sacrificed permanent" over {@code Subjects.departedThisWay(StepKind.SACRIFICE)}
 * (Reweave). Each permanent's card types are those it has on the battlefield, else those it had as a removal step of
 * this resolution moved it (CR 608.2h). The subject needs the resolving entry, so a handler binds the predicate to
 * those card types as the effect applies ({@code SubjectResolutionService.bindCardSubjects}); unbound, or with no
 * permanent named, it matches nothing.
 */
public record CardSharesCardTypeWithPermanentsPredicate(PermanentSubject permanents) implements CardPredicate {

    public CardSharesCardTypeWithPermanentsPredicate {
        Objects.requireNonNull(permanents, "permanents");
    }
}

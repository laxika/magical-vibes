package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.effect.PermanentSubject;

import java.util.Objects;

/**
 * Matches a permanent that shares a creature type with one of the permanents {@code permanents} names: "destroy all
 * creatures that share a creature type with the sacrificed creature" over {@code Subjects.paidAsCost()} (Endemic
 * Plague). The named permanents' creature types are read as {@link CardSharesCreatureTypeWithPermanentsPredicate}
 * reads them, live or as they last existed (CR 608.2h), and fixed once as the effect applies; the handler binds the
 * predicate to them ({@code SubjectResolutionService.bindCardSubjects(GameData, StackEntry, CardEffect,
 * PermanentPredicate)}). Unbound, or with no creature type named, it matches nothing.
 */
public record PermanentSharesCreatureTypeWithPermanentsPredicate(PermanentSubject permanents)
        implements PermanentPredicate {

    public PermanentSharesCreatureTypeWithPermanentsPredicate {
        Objects.requireNonNull(permanents, "permanents");
    }
}

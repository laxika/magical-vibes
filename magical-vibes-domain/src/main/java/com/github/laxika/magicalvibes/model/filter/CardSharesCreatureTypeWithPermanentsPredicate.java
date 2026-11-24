package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.effect.PermanentSubject;

import java.util.Objects;

/**
 * Matches a card that shares a creature type with one of the permanents {@code permanents} names: "a creature card that
 * shares a creature type with the sacrificed creature" over {@code Subjects.paidAsCost()} (Pyre of Heroes), "with
 * enchanted creature" over {@code Subjects.attachedCreature()} (Call to the Kindred), "with that creature" over
 * {@code Subjects.triggering()} (Conjurer's Mantle). Each permanent's creature types are those it has on the
 * battlefield, else those it had as it last existed there (CR 608.2h); a changeling is every creature type
 * (CR 702.73a), and a face-down permanent has only those effects give it (CR 708.2a). The subject needs the resolving
 * entry, so a handler binds the predicate to those creature types as the effect applies
 * ({@code SubjectResolutionService.bindCardSubjects}); unbound, or with no creature type named, it matches nothing.
 */
public record CardSharesCreatureTypeWithPermanentsPredicate(PermanentSubject permanents) implements CardPredicate {

    public CardSharesCreatureTypeWithPermanentsPredicate {
        Objects.requireNonNull(permanents, "permanents");
    }
}

package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.effect.PermanentSubject;

import java.util.Objects;

/**
 * Matches a card with the same name as one of the permanents {@code permanents} names: its negation is "with a
 * different name than each Aura you control" (Light-Paws, Emperor's Voice) and "that doesn't have the same name as a
 * Curse attached to enchanted player" (Curse of Misfortunes). A permanent with no name (face down, CR 708.2) shares
 * it with nothing (CR 201.2a). The subject needs the resolving entry, so a handler binds the predicate to those
 * permanents' names as the effect applies ({@code SubjectResolutionService.bindCardSubjects}); unbound it matches
 * nothing.
 */
public record CardSharesNameWithPermanentsPredicate(PermanentSubject permanents) implements CardPredicate {

    public CardSharesNameWithPermanentsPredicate {
        Objects.requireNonNull(permanents, "permanents");
    }
}

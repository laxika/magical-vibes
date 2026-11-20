package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Objects;
import java.util.Set;

/**
 * One-shot effect: the target, every creature on the battlefield, or every creature controlled by
 * the source's controller becomes the creature type chosen at resolution until end of turn,
 * replacing all other creature types.
 */
public record TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(
        PermanentSubject subject,
        Set<CardSubtype> excludedSubtypes
) implements CardEffect {

    public TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect() {
        this(Subjects.target(), Set.of(CardSubtype.WALL));
    }

    public TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(PermanentSubject subject) {
        this(subject, Set.of(CardSubtype.WALL));
    }

    public TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(PermanentSubject subject,
                                                                   Set<CardSubtype> excludedSubtypes) {
        this.subject = Objects.requireNonNull(subject, "subject");
        this.excludedSubtypes = Set.copyOf(Objects.requireNonNull(excludedSubtypes, "excludedSubtypes"));
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.isSet() ? TargetSpec.NONE : TargetSpec.benign(TargetPredicates.creature());
    }
}

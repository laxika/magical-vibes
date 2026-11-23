package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Objects;
import java.util.Set;

/**
 * One-shot effect: the permanents {@code subject} names (target creature, the source, the permanent the source Aura
 * enchants, or a set found as the effect resolves) become the creature type chosen at resolution until end of turn.
 *
 * @param subject          who becomes the chosen type
 * @param excludedSubtypes creature types that can't be chosen (Wall for the target and all-creatures forms by default)
 * @param retainOtherTypes whether the type is added to their other creature types rather than replacing them
 * @param duration         how long they are that type
 */
public record TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(
        PermanentSubject subject,
        Set<CardSubtype> excludedSubtypes,
        boolean retainOtherTypes,
        Duration duration
) implements CardEffect {

    public TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(PermanentSubject subject,
                                                                   Set<CardSubtype> excludedSubtypes,
                                                                   boolean retainOtherTypes) {
        this(subject, excludedSubtypes, retainOtherTypes, Duration.UNTIL_END_OF_TURN);
    }

    public TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect() {
        this(Subjects.target(), Set.of(CardSubtype.WALL));
    }

    public TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(PermanentSubject subject) {
        this(subject, Set.of(CardSubtype.WALL));
    }

    public TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(PermanentSubject subject,
                                                                   Set<CardSubtype> excludedSubtypes) {
        this(subject, excludedSubtypes, false);
    }

    public TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(duration, "duration");
        excludedSubtypes = Set.copyOf(Objects.requireNonNull(excludedSubtypes, "excludedSubtypes"));
        if (!subject.equals(Subjects.target()) && !subject.equals(Subjects.source())
                && !subject.equals(Subjects.attached()) && (!subject.isSet() || subject.declaresTarget())) {
            throw new IllegalArgumentException("Unsupported type-change subject: " + subject);
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}

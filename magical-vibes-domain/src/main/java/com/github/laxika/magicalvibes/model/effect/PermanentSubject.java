package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Objects;

/**
 * Which permanents an effect acts on: the effect's verb names the action, this value names who it happens to. Built
 * through {@link Subjects}; resolved only by the engine's {@code SubjectResolutionService}, which owns every lookup
 * (current information, else last-known information, and nothing done to an object that left the battlefield).
 *
 * <p>Only {@link Target} declares a target ({@link #declaresTarget()}). A subject that reads a stack entry
 * ({@link #needsStackEntry()}) cannot be used by a static ability.
 *
 * <p>The variants and the wording each stands for are listed in {@code agent-docs/SUBJECTS_REFERENCE.md}.
 */
public sealed interface PermanentSubject permits PermanentSubject.Source, PermanentSubject.Target,
        PermanentSubject.Attached, PermanentSubject.AttachedAtTrigger, PermanentSubject.Triggering, PermanentSubject.TriggeringCard,
        PermanentSubject.Returned, PermanentSubject.ControlledBy, PermanentSubject.AllOf {

    /** Whether this subject is the effect's own target, chosen as the spell or ability is put on the stack. */
    default boolean declaresTarget() {
        return false;
    }

    /** Whether resolving this subject reads a stack entry, so a static ability can't use it. */
    default boolean needsStackEntry() {
        return false;
    }

    /** "This permanent": the permanent the spell's or ability's source is. */
    record Source() implements PermanentSubject {
    }

    /**
     * "Target permanent": the effect's target(s). {@code group} is the target group the effect reads, or {@code null}
     * for the targets the stack entry assigns to the effect.
     */
    record Target(Integer group) implements PermanentSubject {

        public Target {
            if (group != null && group < 0) {
                throw new IllegalArgumentException("A target group is not negative: " + group);
            }
        }

        @Override
        public boolean declaresTarget() {
            return true;
        }

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /** "Enchanted creature", "equipped creature": the permanent the source Aura or Equipment is attached to. */
    record Attached() implements PermanentSubject {
    }

    /**
     * "That creature" in an Aura's or Equipment's ability whose trigger condition names its host ("at the beginning of
     * the upkeep of enchanted creature's controller, ... destroy that creature"): the permanent the source was attached
     * to when the ability triggered or was activated, as the stack entry recorded it, not whatever it is attached to
     * at resolution.
     */
    record AttachedAtTrigger() implements PermanentSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /** "It", "that creature": the permanent the trigger event was about. */
    record Triggering() implements PermanentSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "That card" of a "whenever you play a card" event: the spell cast or the land played that the trigger event was
     * about, which need not be a permanent. Its permanent is the land on the battlefield; {@code ownerOf} reads the
     * card's owner wherever the card is.
     */
    record TriggeringCard() implements PermanentSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "That creature" after "return target card from a graveyard to the battlefield": the permanent the returned card
     * became. A step result: it reads what an earlier step of the same resolution did.
     */
    record Returned() implements PermanentSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "Each creature you control", "all creatures": every permanent matching {@code filter} that one of
     * {@code players} controls, found once when the effect is applied.
     */
    record ControlledBy(PlayerSubject players, PermanentPredicate filter) implements PermanentSubject {

        public ControlledBy {
            Objects.requireNonNull(players, "players");
            Objects.requireNonNull(filter, "filter (use PermanentTruePredicate for any permanent)");
        }

        @Override
        public boolean needsStackEntry() {
            return players.needsStackEntry();
        }
    }

    /** Several subjects together ("this creature and target creature"), each permanent once. */
    record AllOf(List<PermanentSubject> subjects) implements PermanentSubject {

        public AllOf {
            subjects = List.copyOf(subjects);
            if (subjects.size() < 2) {
                throw new IllegalArgumentException("AllOf joins at least two subjects: " + subjects);
            }
            if (subjects.stream().filter(PermanentSubject::declaresTarget).count() > 1) {
                throw new IllegalArgumentException("AllOf holds at most one target subject: " + subjects);
            }
        }

        @Override
        public boolean declaresTarget() {
            return subjects.stream().anyMatch(PermanentSubject::declaresTarget);
        }

        @Override
        public boolean needsStackEntry() {
            return subjects.stream().anyMatch(PermanentSubject::needsStackEntry);
        }
    }
}

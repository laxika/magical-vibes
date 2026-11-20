package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Which permanents an effect acts on: the effect's verb names the action, this value names who it happens to. Built
 * through {@link Subjects}; resolved only by the engine's {@code SubjectResolutionService}, which owns every lookup
 * (current information, else last-known information, and nothing done to an object that left the battlefield).
 *
 * <p>Only {@link Target} and {@link AllTargets} declare a target ({@link #declaresTarget()}). A subject that reads a
 * stack entry ({@link #needsStackEntry()}) cannot be used by a static ability.
 *
 * <p>The variants and the wording each stands for are listed in {@code agent-docs/SUBJECTS_REFERENCE.md}.
 */
public sealed interface PermanentSubject permits PermanentSubject.Source, PermanentSubject.Target, PermanentSubject.AllTargets,
        PermanentSubject.Attached, PermanentSubject.AttachedOrSource, PermanentSubject.AttachedAtTrigger, PermanentSubject.Triggering, PermanentSubject.TriggeringOrSource, PermanentSubject.TriggeringCard,
        PermanentSubject.TargetSpell, PermanentSubject.TriggeringSpell, PermanentSubject.Returned, PermanentSubject.Granter, PermanentSubject.Named,
        PermanentSubject.ControlledBy, PermanentSubject.OwnedBy, PermanentSubject.AttachmentsOf, PermanentSubject.AllOf {

    /** Whether this subject is the effect's own target, chosen as the spell or ability is put on the stack. */
    default boolean declaresTarget() {
        return false;
    }

    /** Whether resolving this subject reads a stack entry, so a static ability can't use it. */
    default boolean needsStackEntry() {
        return false;
    }

    /**
     * Whether this subject is a set found as the effect is applied ("all creatures you control"), rather than
     * permanents it names one by one.
     */
    default boolean isSet() {
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

    /**
     * "Up to four target permanents" chosen across several target groups: every permanent target of the stack entry,
     * whichever group or effect it was chosen for (an ability whose targets are declared by its target filter or by
     * several slots, not bound to one group).
     */
    record AllTargets() implements PermanentSubject {

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
     * "This creature ... If it's an Aura, enchanted creature ... instead" (a bestow card's own ability): the permanent
     * the source is attached to while it is attached, else the source itself; read from the source as it is, else as
     * it last existed.
     */
    record AttachedOrSource() implements PermanentSubject {
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
     * "It" after "whenever this creature or equipped creature attacks" (a reconfigure card's own trigger): the
     * permanent the trigger event recorded (the equipped creature), else, for the source's own event, which records
     * none, the source itself.
     */
    record TriggeringOrSource() implements PermanentSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "That card" of a "whenever you play a card" event: the spell cast or the land played that the trigger event was
     * about, which need not be a permanent. Its permanent is the land on the battlefield; {@code ownerOf} reads the
     * card's owner wherever the card is, and for an event about no spell or land, the owner of the card or permanent
     * the event was about (a card put into a graveyard, a permanent that entered, as it is or last was).
     */
    record TriggeringCard() implements PermanentSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "Target spell", read inside {@code controllerOf} ("that spell's controller"): the spell the stack entry targets,
     * which is no permanent, so it names no permanent on the battlefield. Its controller is read from the stack while
     * the spell is there, else as the resolving effect last saw it (countered or removed by an earlier step). It does
     * not declare the target; the effect that reads it does.
     */
    record TargetSpell() implements PermanentSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "That spell or ability", read inside {@code controllerOf}: the spell or ability a trigger event was about (it
     * became the target of it, it was cast), as it is on the stack, else as it last existed there (CR 608.2h). It is
     * no permanent, so it names no permanent on the battlefield.
     */
    record TriggeringSpell() implements PermanentSubject {

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
     * "Return this Equipment to its owner's hand" in an ability the Equipment grants: the permanent that granted the
     * resolving ability, not the creature that has it. {@code boundPermanentId} is the granting permanent an activated
     * ability recorded as it was activated ({@code null} until then); a granted triggered ability's entry records it
     * itself.
     */
    record Granter(UUID boundPermanentId) implements PermanentSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "That creature" in a delayed triggered ability a resolving ability created about another permanent than its own
     * trigger event ("when this leaves the battlefield or becomes untapped, remove all -1/-1 counters from that
     * creature", Giant Oyster): the permanent {@code permanentId} the creating effect bound as it resolved. A permanent
     * that left the battlefield and came back is a new object with a new id, so it is not named (CR 603.7c).
     */
    record Named(UUID permanentId) implements PermanentSubject {

        public Named {
            Objects.requireNonNull(permanentId, "permanentId");
        }

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

        @Override
        public boolean isSet() {
            return true;
        }
    }

    /**
     * "All artifacts target player owns": every permanent on the battlefield matching {@code filter} whose owner is one
     * of {@code players}, whoever controls it, found once when the effect is applied.
     */
    record OwnedBy(PlayerSubject players, PermanentPredicate filter) implements PermanentSubject {

        public OwnedBy {
            Objects.requireNonNull(players, "players");
            Objects.requireNonNull(filter, "filter (use PermanentTruePredicate for any permanent)");
        }

        @Override
        public boolean needsStackEntry() {
            return players.needsStackEntry();
        }

        @Override
        public boolean isSet() {
            return true;
        }
    }

    /**
     * "All Auras attached to target permanent": every permanent matching {@code filter} attached to one of the
     * permanents {@code permanents} names, whoever controls it, found once when the effect is applied. The
     * attachments are not targeted; a target in {@code permanents} is declared by the effect that reads it.
     */
    record AttachmentsOf(PermanentSubject permanents, PermanentPredicate filter) implements PermanentSubject {

        public AttachmentsOf {
            Objects.requireNonNull(permanents, "permanents");
            Objects.requireNonNull(filter, "filter (use PermanentTruePredicate for any attachment)");
        }

        @Override
        public boolean needsStackEntry() {
            return permanents.needsStackEntry();
        }

        @Override
        public boolean isSet() {
            return true;
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

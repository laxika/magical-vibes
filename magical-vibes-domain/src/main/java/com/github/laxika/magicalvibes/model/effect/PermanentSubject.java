package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StepKind;
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
        PermanentSubject.Attached, PermanentSubject.AttachedOrSource, PermanentSubject.AttachedAtTrigger, PermanentSubject.Triggering, PermanentSubject.TriggeringAll, PermanentSubject.TriggeringOrSource, PermanentSubject.TriggeringCard,
        PermanentSubject.TargetSpell, PermanentSubject.TargetAbilitySource, PermanentSubject.TriggeringSpell, PermanentSubject.Returned,
        PermanentSubject.ReturnedThisResolution, PermanentSubject.ExiledThisResolution, PermanentSubject.Granter,
        PermanentSubject.Named, PermanentSubject.ControlledBy, PermanentSubject.OwnedBy, PermanentSubject.AttachmentsOf,
        PermanentSubject.AllOf, PermanentSubject.Except, PermanentSubject.OneOf, PermanentSubject.TriggeringAttackers,
        PermanentSubject.TriggeringSpellTargets, PermanentSubject.CreatedThisResolution, PermanentSubject.BandmatesOf,
        PermanentSubject.WithCreaturesSharingAColor, PermanentSubject.OtherPermanentsSharing, PermanentSubject.AttachedAndSharingCreatureType,
        PermanentSubject.Searched, PermanentSubject.ThisWay, PermanentSubject.Voted {

    /** Whether this subject is the effect's own target, chosen as the spell or ability is put on the stack. */
    default boolean declaresTarget() {
        return false;
    }

    /** Whether resolving this subject reads a stack entry, so a static ability can't use it. */
    default boolean needsStackEntry() {
        return false;
    }

    /**
     * Whether this subject is a set of the permanents a player target of the effect controls ("creatures target player
     * controls"), so the effect declares that player as its target.
     */
    default boolean declaresPlayerTarget() {
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

    /**
     * "Enchanted permanent", "enchanted creature", "equipped creature": the permanent the source Aura or Equipment is
     * attached to. With {@code creature}, "enchanted creature" / "equipped creature": a resolving effect names the host
     * only while it is a creature. A static ability's host is judged without a type read (an Aura or Equipment on a
     * noncreature it can't be attached to leaves it as state-based actions are checked).
     */
    record Attached(boolean creature) implements PermanentSubject {

        /** "Enchanted permanent": the host, whatever it is. */
        public Attached() {
            this(false);
        }
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
     * "Them", "each of them": every permanent the trigger event was about, the group of a "one or more" event (CR 603.2c),
     * else its one permanent.
     */
    record TriggeringAll() implements PermanentSubject {

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
     * "That permanent" after "counter target activated or triggered ability": the permanent the ability the stack entry
     * targets came from, read from the stack while that ability is there, else as the counter step recorded it as it
     * countered it. Nothing for a spell target, an ability with no source permanent, or a permanent no longer on the
     * battlefield as the same object (CR 400.7). {@code filter} narrows it to a permanent matching it as the reading
     * effect is applied ("if an ability of an artifact, creature, or planeswalker is countered this way"); {@code null}
     * names it whatever it is. It does not declare the target; the counter effect beside it does.
     */
    record TargetAbilitySource(PermanentPredicate filter) implements PermanentSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "That spell or ability", read inside {@code controllerOf}: the spell or ability a trigger event was about (it
     * became the target of it, it was cast), as it is on the stack, else as it last existed there (CR 608.2h). It is
     * no permanent, so it names no permanent on the battlefield. A duplicate of it ("conjure a duplicate of that card")
     * is of the spell's card.
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
     * "Exile them again" after an earlier step of the same resolution returned permanents to the battlefield: every
     * permanent that step recorded on the stack entry as returned (Lae'zel's Acrobatics). A step result.
     */
    record ReturnedThisResolution() implements PermanentSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "Return that card" after an earlier step of the same resolution exiled the effect's target: the card that step
     * exiled, which is no permanent, so it names none on the battlefield; its id (the entry's target) leads to the card
     * the entry remembered as the permanent left. Declares no target (Gilraen, Dúnedain Protector). A step result.
     */
    record ExiledThisResolution() implements PermanentSubject {

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
     * "That creature" an engine-built effect bound to one permanent as it resolved (the attacker a delayed "phase it out
     * at end of combat" names, the creature an Aura returned with is attached to): the permanent {@code permanentId}.
     * A permanent that left the battlefield and came back is a new object with a new id, so it is not named
     * (CR 603.7c).
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
        public boolean declaresPlayerTarget() {
            return players.declaresTarget();
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

    /**
     * The permanents {@code permanents} names but those {@code excluded} names: "then exile all other Nightmare tokens
     * you control" after creating a token is {@code except(controlledBy(you(), Nightmare token), createdThisResolution())}.
     * Neither part declares a target, save {@link AllTargets} as the permanents: "the other" of the targets after a
     * step that chose one of them is {@code except(allTargets(), thisWay(CHOOSE))}, the targets being declared by the
     * spell or ability's target groups (Retribution, Barrin's Spite); the exception itself declares none.
     */
    record Except(PermanentSubject permanents, PermanentSubject excluded) implements PermanentSubject {

        public Except {
            Objects.requireNonNull(permanents, "permanents");
            Objects.requireNonNull(excluded, "excluded");
            if (permanents.declaresTarget() && !(permanents instanceof AllTargets) || excluded.declaresTarget()) {
                throw new IllegalArgumentException("An exception can't declare a target: " + permanents + ", "
                        + excluded);
            }
        }

        @Override
        public boolean needsStackEntry() {
            return permanents.needsStackEntry() || excluded.needsStackEntry();
        }

        @Override
        public boolean isSet() {
            return permanents.isSet();
        }
    }

    /**
     * "Exile this permanent or up to one target creature": one of the permanents {@code options} name, which the
     * controller chooses as the effect resolves. The resolver does not choose; the verb's handler resolves each option
     * and asks. A target option's target is declared by the ability that holds the effect, not by this subject.
     */
    record OneOf(List<PermanentSubject> options) implements PermanentSubject {

        public OneOf {
            options = List.copyOf(options);
            if (options.size() < 2) {
                throw new IllegalArgumentException("OneOf offers at least two subjects: " + options);
            }
        }

        @Override
        public boolean needsStackEntry() {
            return options.stream().anyMatch(PermanentSubject::needsStackEntry);
        }
    }

    /**
     * "They gain ...", "those creatures" in a "whenever you attack" ability: the attacking creatures the stack entry
     * recorded as the ability triggered, those still on the battlefield as it resolves. No target.
     */
    record TriggeringAttackers() implements PermanentSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "Those creatures" naming the targets of the spell a trigger event was about: that spell's targets as it is on the
     * stack, else as the entry recorded them as the ability triggered; with no triggering spell, the targets the
     * resolving entry assigns to the effect (its own targets, declared by the card's target filter, not by this
     * subject).
     */
    record TriggeringSpellTargets() implements PermanentSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "Those tokens" after an earlier step of the same resolution created them: every permanent the stack entry
     * recorded as created this resolution, those still on the battlefield. A step result.
     */
    record CreatedThisResolution() implements PermanentSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "Enchanted creature and other creatures that share a creature type with it" in an ability of the attachment
     * itself (the Crowns): the host recorded as the ability was activated, before a cost detached the attachment, as it
     * is or last was (CR 608.2h), and every creature sharing a creature type with it, found as the effect resolves;
     * nothing while the host is on the battlefield and no creature. An effect reading it resolves against the attached
     * permanent ({@link CardEffect#resolvesAgainstAttachedPermanent}).
     */
    record AttachedAndSharingCreatureType() implements PermanentSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }

        @Override
        public boolean isSet() {
            return true;
        }
    }

    /**
     * "It", "that land", "those lands" after "search your library for ..., put it onto the battlefield": the
     * permanents the entry recorded the search as having put onto the battlefield, those still there. Also the card a
     * look at the top of a library put onto the battlefield ("you may put a creature card from among them onto the
     * battlefield, then ... that card"), which the entry records as its chosen permanent.
     */
    record Searched() implements PermanentSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "Those creatures", "each creature dealt damage this way": the permanents the most recent {@code kind} step of the
     * same resolution acted on, as the stack entry's resolution scratchpad recorded them, those still on the
     * battlefield as the same objects (CR 400.7). Only for the kinds whose objects stay on the battlefield
     * ({@link StepKind#PUT_COUNTERS}, {@link StepKind#REMOVE_COUNTERS}, {@link StepKind#DEAL_DAMAGE},
     * {@link StepKind#MANIFEST}: "put a +1/+1 counter on it" after a manifest, {@link StepKind#CHOOSE}: "those
     * creatures" after {@code ChoosePermanentsEffect}); tokens created
     * this way are {@link CreatedThisResolution} and permanents returned this way {@link ReturnedThisResolution}. A step
     * result.
     *
     * <p>{@code filter} narrows them to those matching it as the reading effect is applied ("if a Dragon is dealt damage
     * this way, destroy it", "a creature dealt damage this way can't block"); {@code null} names every one.
     */
    record ThisWay(StepKind kind, PermanentPredicate filter) implements PermanentSubject {

        public ThisWay(StepKind kind) {
            this(kind, null);
        }

        public ThisWay {
            Objects.requireNonNull(kind, "kind");
            switch (kind) {
                case PUT_COUNTERS, REMOVE_COUNTERS, DEAL_DAMAGE, MANIFEST, CHOOSE -> {
                }
                case CREATE_TOKEN -> throw new IllegalArgumentException(
                        "Tokens created this way are Subjects.createdThisResolution()");
                case RETURN -> throw new IllegalArgumentException(
                        "Permanents returned this way are Subjects.returnedThisResolution()");
                default -> throw new IllegalArgumentException("A " + kind + " step leaves no permanent to name; its "
                        + "players are Subjects.playersThisWay(" + kind + ")");
            }
        }

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "Each other creature banded with it" (CR 702.22): the other attacking creatures in the same attacking band as
     * the one permanent {@code permanent} names, read as it is, else (for {@code source()}) as it last existed. Nothing
     * when it is not attacking in a band. Found as the effect resolves.
     */
    record BandmatesOf(PermanentSubject permanent) implements PermanentSubject {

        public BandmatesOf {
            Objects.requireNonNull(permanent, "permanent");
        }

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "Target creature and each other creature that shares a color with it": the creature {@code permanent} names and
     * every creature that shares a color with it, found as the effect resolves; nothing when the named permanent is no
     * longer a creature. Declares a target only through {@code permanent}.
     */
    record WithCreaturesSharingAColor(PermanentSubject permanent) implements PermanentSubject {

        public WithCreaturesSharingAColor {
            Objects.requireNonNull(permanent, "permanent");
        }

        @Override
        public boolean declaresTarget() {
            return permanent.declaresTarget();
        }

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "Each other creature that shares a color with it", "all other permanents with the same name as that permanent",
     * "each other nonland permanent with the same mana value as that permanent": every permanent matching
     * {@code filter} other than the one {@code permanent} names that shares {@code quality} with it, found as the
     * effect resolves; nothing when the named permanent is gone or no longer matches {@code filter}. With
     * {@code sameController} ("all other creatures that player controls with the same name", "all tokens its controller
     * controls with the same name") only its controller's, and {@code filter} describes just those others. It only reads
     * {@code permanent} (a {@code target()} there is the target another effect declares), so it declares no target;
     * "target creature and …" is the target's own effect beside it, or an {@code allOf} with it.
     */
    record OtherPermanentsSharing(PermanentSubject permanent, SharedQuality quality, PermanentPredicate filter,
                                  boolean sameController) implements PermanentSubject {

        /** The others anyone controls. */
        public OtherPermanentsSharing(PermanentSubject permanent, SharedQuality quality, PermanentPredicate filter) {
            this(permanent, quality, filter, false);
        }

        /** What the other permanents share with the named one. */
        public enum SharedQuality {
            /** At least one color; a colorless permanent shares a color with nothing. */
            COLOR,
            /**
             * The name; a permanent with no name (face down, CR 708.2) shares it with nothing, and one that has all
             * card names shares it with every permanent that has a name.
             */
            NAME,
            /** At least one creature type. */
            CREATURE_TYPE,
            /** The mana value (a face-down permanent's is 0; a transformed permanent's is its front face's). */
            MANA_VALUE
        }

        public OtherPermanentsSharing {
            Objects.requireNonNull(permanent, "permanent");
            Objects.requireNonNull(quality, "quality");
            Objects.requireNonNull(filter, "filter");
        }

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "Each permanent with the most votes or tied for most votes" ({@link VoteEffect.Tally#MOST}), "each creature with one
     * or more votes" ({@link VoteEffect.Tally#ANY}): the permanents the most recent vote of the same resolution
     * ({@code VoteEffect}) was cast for, by the votes each received, those still on the battlefield as the same objects
     * (CR 400.7). A candidate with no votes never has the most votes, so nothing is named when no vote was cast. A step
     * result.
     *
     * @param tally which voted permanents are named
     */
    record Voted(VoteEffect.Tally tally) implements PermanentSubject {

        public Voted {
            Objects.requireNonNull(tally, "tally");
            if (tally == VoteEffect.Tally.NONE) {
                throw new IllegalArgumentException("The permanents that received no votes are not recorded");
            }
        }

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }
}

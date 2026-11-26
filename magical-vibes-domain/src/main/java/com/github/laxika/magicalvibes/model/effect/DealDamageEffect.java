package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTargetCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Objects;

/**
 * One source deals {@code damage}: "deals N damage to any target", "to target creature", "to target creature or
 * planeswalker", "to target player or planeswalker", "to target permanent", to the one permanent a non-target
 * {@code subject} names ("to itself", "to it", "to enchanted creature"), to the players a {@link PlayerSubject} names
 * ("to each opponent", "to you", "to that player or a planeswalker that player controls"), to each permanent of a set
 * ("to each creature target player controls", {@link #toEach}; "to each creature [and each player]",
 * {@link #toEachCreature}) or to each of its targets ("to each of up to three targets", {@link #toEachTarget}), the set
 * (with its players) or the targets in one damage event. The amount is a {@link DynamicAmount} evaluated as it resolves, against the dealing permanent as it is
 * or last existed.
 *
 * <p>Build it with the factories, which read like the wording: {@link #toAnyTarget}, {@link #toTargetCreature},
 * {@link #toTargetCreatureOrPlaneswalker}, {@link #toTargetPlayerOrPlaneswalker},
 * {@link #toTargetOpponentOrPlaneswalker}, {@link #toTargetPermanent}, {@link #to(PermanentSubject, int)}, the
 * player factories ({@link #to(PlayerSubject, int)}, {@link #eachEvaluatedFor}, {@link #toPlayerOrTheirPlaneswalker}
 * and the rider {@link #byEventDamageSource}), the set factories ({@link #toEach(PermanentSubject, int)},
 * {@link #toEachCreatureOfTargetedPlayer}, {@link #toEachControlledByTargetedPlayer(int, PermanentPredicate)},
 * {@link #toCreaturesOfTargetedPlayer}, {@link #toEachTarget(int)}, {@link #toEachTargetCreature},
 * {@link #toEachTargetCreatureOrPlaneswalker}, the mass factories {@link #toEachCreature(int)},
 * {@link #toEachCreature(int, PermanentPredicate)}, {@link #toEachCreatureAndPlaneswalker(int)},
 * {@link #toEachCreatureAndPlaneswalker(int, PermanentPredicate)}, {@link #eachEvaluatedFor(DynamicAmount,
 * PermanentSubject)} and the rider {@link #andTo}), the
 * any-target markers ({@link #toAnyOtherTarget}, {@link #toEventObject}, {@link #toAnyTargetOfAnOpponentsChoice},
 * {@link #toAnyTargetChosenAtRandom}, {@link #toAnyTargetInGroup}), and the riders chained after one
 * ({@link #byTriggeringPermanent}, {@link #unpreventable}, {@link #unpreventableWhen}, {@link #cantBeRegenerated},
 * {@link #withUnconditionalRegenerationPrevention}, {@link #exilingDamagedCreaturesThatWouldDie},
 * {@link #exilingIfItWouldDie}, {@link #exilingPermanentsDealtDamage}, {@link #recordingDamageDealt}).
 *
 * <p>A set over every player's permanents ({@code controlledBy(eachPlayer(), …)}) is a board sweep for the AI
 * ({@link #sweepsBoard()}).
 *
 * <p>Subjects other than {@link Subjects#target()} (creature recipient only) target nothing (CR 115.10a), so
 * protection's targeting half and hexproof don't stop them:
 * <ul>
 *   <li>{@link Subjects#source()}: "and X damage to itself" (Sunflare Shaman), dealt to the live source only;</li>
 *   <li>{@link Subjects#triggering()}: "deals 4 damage to it" (Aether Flash), the permanent the trigger event was
 *       about if it is still a creature, planeswalker or battle;</li>
 *   <li>{@link Subjects#attached()} / {@link Subjects#attachedAtTrigger()}: an Aura's "deals 2 damage to enchanted
 *       creature" (Fatal Attraction) / "to that creature" (Guilty Conscience), the Aura, as it is or last existed,
 *       dealing it;</li>
 *   <li>{@link Subjects#thisWay}: "deals 2 damage to [the creature chosen]" after a choose step (Purging Scythe).</li>
 * </ul>
 *
 * @param damage                            the amount of damage to deal
 * @param recipient                         what may be dealt the damage, which fixes the target spec
 * @param subject                           the permanent dealt the damage; {@link Subjects#target()} except for a
 *                                          non-target creature subject; the set for {@link Recipient#EACH_PERMANENT};
 *                                          {@code null} for the player kinds
 * @param targetFilter                      an extra restriction on a targeted creature or creature-or-planeswalker
 *                                          ("target creature an opponent controls"), or on the permanents each target
 *                                          may be ({@link Recipient#EACH_TARGET}); {@code null} for none
 * @param playerRelation                    {@link PlayerRelation#OPPONENT} for "target opponent or planeswalker";
 *                                          {@link PlayerRelation#ANY} otherwise. The narrowing isn't expressible in
 *                                          the spec: {@code DamageTargetValidators} enforces it
 * @param targetGroup                       any target only: {@code -1} the entry's target, {@code >= 0} that target
 *                                          group's chosen target, the negative markers of the any-target factories
 * @param damageSource                      what deals the damage: the ability's own source, or the permanent the
 *                                          trigger event was about ("it deals", CR 608.2h; a set is then found with
 *                                          that permanent as the source: "each creature blocking it"), or for the
 *                                          player kinds the permanent that dealt the trigger event's damage
 * @param alwaysUnpreventable               "the damage can't be prevented" (CR 615.12), for this one damage event
 * @param unpreventableWhen                 when met as it resolves, this damage can't be prevented (CR 615.12):
 *                                          Banefire's {@code SpellXAtLeast(5)}; {@code null} for none
 * @param cantBeRedirectedWhenUnpreventable damage made unpreventable by {@code unpreventableWhen} also can't be
 *                                          redirected (Lava Burst)
 * @param cantRegenerate                    a creature dealt damage this way can't be regenerated this turn
 *                                          (Incinerate, Flamebreak); any target and each permanent of a set only
 * @param preventRegenerationWithoutDamage  the targeted creature can't be regenerated even if no damage is dealt
 *                                          (Disintegrate); any target only
 * @param exile                             which permanent is exiled instead if it would die this turn
 * @param recordDamageDealt                 any target and players only: the damage actually dealt is the entry's
 *                                          event value for later effects (Corrupt, Syphon Soul)
 * @param players                           the player kinds: the players dealt the damage, resolved by
 *                                          {@code SubjectResolutionService} (several in APNAP order);
 *                                          {@link Recipient#EACH_PERMANENT}: the players also dealt the damage in the
 *                                          set's damage event ("and each player", {@link #andTo}), else {@code null}
 * @param amountForEachRecipient            the amount is evaluated for each recipient before any damage is dealt
 *                                          (CR 608.2h): for the player kinds with that player as the controller, for
 *                                          {@link Recipient#EACH_PERMANENT} with that permanent as the amount's source
 *                                          ({@link #eachEvaluatedFor})
 */
public record DealDamageEffect(DynamicAmount damage, Recipient recipient, PermanentSubject subject,
                               PermanentPredicate targetFilter, PlayerRelation playerRelation, int targetGroup,
                               DamageSource damageSource, boolean alwaysUnpreventable, Condition unpreventableWhen,
                               boolean cantBeRedirectedWhenUnpreventable, boolean cantRegenerate,
                               boolean preventRegenerationWithoutDamage, ExileIfItWouldDie exile,
                               boolean recordDamageDealt, PlayerSubject players, boolean amountForEachRecipient)
        implements DamageDealingEffect, BoardWipeEffect {

    /** What a damage effect may be dealt to; each fixes its target spec. */
    public enum Recipient {
        /** "Any target": a creature, planeswalker, battle or player (CR 115.4). */
        ANY_TARGET,
        /** "Target creature", or the one permanent a non-target subject names. */
        CREATURE,
        /** "Target creature or planeswalker". */
        CREATURE_OR_PLANESWALKER,
        /** "Target player or planeswalker" / "target opponent or planeswalker". */
        PLAYER_OR_PLANESWALKER,
        /** "Target permanent" that can be dealt damage. */
        PERMANENT,
        /**
         * The players {@code players} names ("to each opponent", "to you", "to that player", "to target player"):
         * a target only when the subject declares one.
         */
        PLAYERS,
        /**
         * The player {@code players} names or a planeswalker that player controls, chosen without targeting as it
         * resolves ("to that player or a planeswalker that player controls").
         */
        PLAYER_OR_THEIR_PLANESWALKER,
        /**
         * Each permanent the non-target set {@code subject} names ("to each creature target player controls", "to each
         * attacking creature", "to each other creature that shares a color with it"), found once as the effect is
         * applied (CR 608.2h) and dealt the damage in one damage event.
         */
        EACH_PERMANENT,
        /**
         * Each target of the effect ("to each of up to three targets"): creatures, players, planeswalkers or battles
         * (CR 115.4), {@code targetFilter} narrowing the permanents that may be targeted; one damage event.
         */
        EACH_TARGET;

        /** Whether this kind is dealt to the players a {@link PlayerSubject} names. */
        public boolean namesPlayers() {
            return this == PLAYERS || this == PLAYER_OR_THEIR_PLANESWALKER;
        }

        /** Whether this kind deals the damage to a set of permanents or to several targets at once. */
        public boolean dealsToSeveral() {
            return this == EACH_PERMANENT || this == EACH_TARGET;
        }
    }

    /** Which permanent a damage effect exiles instead if it would die this turn. */
    public enum ExileIfItWouldDie {
        NONE,
        /**
         * "If that creature [or planeswalker] would die this turn, exile it instead": the target, marked as the
         * effect resolves whether or not it is dealt damage (an any target only while it is a creature).
         */
        TARGET,
        /** "If a creature dealt damage this way would die this turn, exile it instead" (Red Sun's Zenith). */
        DAMAGED_CREATURE,
        /** "If a permanent dealt damage this way would die this turn, exile it instead" (Torch the Tower). */
        DAMAGED_PERMANENT
    }

    private static final PermanentPredicate CREATURE_OR_PLANESWALKER = new PermanentAnyOfPredicate(List.of(
            new PermanentIsCreaturePredicate(), new PermanentIsPlaneswalkerPredicate()));

    private static final int ANY_OTHER_TARGET = -2;
    private static final int EVENT_OBJECT = -3;
    private static final int OF_AN_OPPONENTS_CHOICE = -4;
    private static final int CHOSEN_AT_RANDOM = -5;

    public DealDamageEffect {
        playerRelation = playerRelation == null ? PlayerRelation.ANY : playerRelation;
        damageSource = damageSource == null ? DamageSource.ABILITY_SOURCE : damageSource;
        exile = exile == null ? ExileIfItWouldDie.NONE : exile;
        if (recipient.namesPlayers()) {
            if (players == null || subject != null) {
                throw new IllegalArgumentException("Player damage names its players and no permanent subject");
            }
            if (targetFilter != null || playerRelation != PlayerRelation.ANY || targetGroup != -1
                    || unpreventableWhen != null || cantBeRedirectedWhenUnpreventable || cantRegenerate
                    || preventRegenerationWithoutDamage || exile != ExileIfItWouldDie.NONE
                    || recordDamageDealt && recipient != Recipient.PLAYERS) {
                throw new IllegalArgumentException("Player damage takes no target filter, relation, target group, "
                        + "regeneration or exile rider: " + recipient);
            }
        } else if (recipient.dealsToSeveral()) {
            if (recipient == Recipient.EACH_TARGET && (players != null || amountForEachRecipient)) {
                throw new IllegalArgumentException("Damage to each target names no players: " + recipient);
            }
            if (players != null && amountForEachRecipient) {
                throw new IllegalArgumentException("A per-permanent amount is dealt to no players beside the set");
            }
            subject = subject == null ? Subjects.target() : subject;
            requireSetComponents(recipient, subject, targetFilter, playerRelation, targetGroup, damageSource,
                    alwaysUnpreventable, unpreventableWhen, cantRegenerate, preventRegenerationWithoutDamage, exile,
                    recordDamageDealt);
        } else {
            if (players != null || amountForEachRecipient) {
                throw new IllegalArgumentException("Only player damage names players: " + recipient);
            }
            subject = subject == null ? Subjects.target() : subject;
            requirePermanentRecipientComponents(recipient, subject, targetFilter, playerRelation, targetGroup,
                    damageSource, alwaysUnpreventable, unpreventableWhen, cantRegenerate,
                    preventRegenerationWithoutDamage, exile, recordDamageDealt);
        }
    }

    private static void requireSetComponents(Recipient recipient, PermanentSubject subject,
                                             PermanentPredicate targetFilter, PlayerRelation playerRelation,
                                             int targetGroup, DamageSource damageSource, boolean alwaysUnpreventable,
                                             Condition unpreventableWhen, boolean cantRegenerate,
                                             boolean preventRegenerationWithoutDamage, ExileIfItWouldDie exile,
                                             boolean recordDamageDealt) {
        if (recipient == Recipient.EACH_PERMANENT && subject.declaresTarget()) {
            throw new IllegalArgumentException("Damage to each permanent of a set names the set, not a target: "
                    + subject);
        }
        if (recipient == Recipient.EACH_TARGET && (!subject.equals(Subjects.target())
                || damageSource != DamageSource.ABILITY_SOURCE)) {
            throw new IllegalArgumentException("Damage to each target is dealt by the source to its targets");
        }
        if (recipient == Recipient.EACH_PERMANENT && targetFilter != null) {
            throw new IllegalArgumentException("Damage to each permanent of a set takes its filter in the set");
        }
        if (recipient == Recipient.EACH_TARGET && (cantRegenerate || exile != ExileIfItWouldDie.NONE)) {
            throw new IllegalArgumentException("Damage to each target takes no regeneration or exile rider");
        }
        if (damageSource == DamageSource.EVENT_DAMAGE_SOURCE || playerRelation != PlayerRelation.ANY
                || targetGroup != -1 || alwaysUnpreventable || unpreventableWhen != null
                || preventRegenerationWithoutDamage || exile == ExileIfItWouldDie.TARGET || recordDamageDealt) {
            throw new IllegalArgumentException("Damage to several permanents or targets takes no relation, target "
                    + "group, unpreventable, target exile or recording rider: " + recipient);
        }
    }

    private static void requirePermanentRecipientComponents(Recipient recipient, PermanentSubject subject,
                                                            PermanentPredicate targetFilter,
                                                            PlayerRelation playerRelation, int targetGroup,
                                                            DamageSource damageSource, boolean alwaysUnpreventable,
                                                            Condition unpreventableWhen, boolean cantRegenerate,
                                                            boolean preventRegenerationWithoutDamage,
                                                            ExileIfItWouldDie exile, boolean recordDamageDealt) {
        if (damageSource == DamageSource.EVENT_DAMAGE_SOURCE) {
            throw new IllegalArgumentException("Only player damage is dealt by the event's damage source");
        }
        if (!subject.equals(Subjects.target())) {
            if (recipient != Recipient.CREATURE) {
                throw new IllegalArgumentException("Only creature damage takes a non-target subject: " + subject);
            }
            if (!(subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.Triggering
                    || subject instanceof PermanentSubject.Attached
                    || subject instanceof PermanentSubject.AttachedAtTrigger
                    || subject instanceof PermanentSubject.ThisWay)) {
                throw new IllegalArgumentException("Unsupported damage subject: " + subject);
            }
            if (alwaysUnpreventable || unpreventableWhen != null || targetFilter != null || damageSource != DamageSource.ABILITY_SOURCE
                    || exile != ExileIfItWouldDie.NONE) {
                throw new IllegalArgumentException("Only a target subject takes a target filter, unpreventable "
                        + "damage, an exile rider or another damage source: " + subject);
            }
        }
        if (playerRelation == PlayerRelation.SELF
                || playerRelation != PlayerRelation.ANY && recipient != Recipient.PLAYER_OR_PLANESWALKER) {
            throw new IllegalArgumentException("Only player-or-planeswalker damage takes an opponent relation");
        }
        if (targetFilter != null && recipient != Recipient.CREATURE && recipient != Recipient.CREATURE_OR_PLANESWALKER) {
            throw new IllegalArgumentException("Only creature or creature-or-planeswalker damage takes a target filter");
        }
        if (recipient != Recipient.ANY_TARGET && (targetGroup != -1 || cantRegenerate
                || preventRegenerationWithoutDamage || recordDamageDealt)) {
            throw new IllegalArgumentException("Target groups, regeneration riders and the recorded damage are any "
                    + "target only");
        }
        if (exile == ExileIfItWouldDie.DAMAGED_CREATURE && recipient != Recipient.ANY_TARGET
                || exile == ExileIfItWouldDie.DAMAGED_PERMANENT && recipient != Recipient.CREATURE_OR_PLANESWALKER
                || exile == ExileIfItWouldDie.TARGET && recipient != Recipient.CREATURE_OR_PLANESWALKER
                && !(recipient == Recipient.ANY_TARGET && preventRegenerationWithoutDamage)) {
            throw new IllegalArgumentException("Unsupported exile rider " + exile + " for " + recipient);
        }
    }

    private static DealDamageEffect of(DynamicAmount damage, Recipient recipient) {
        return new DealDamageEffect(damage, recipient, Subjects.target(), null, PlayerRelation.ANY, -1,
                DamageSource.ABILITY_SOURCE, false, null, false, false, false, ExileIfItWouldDie.NONE, false, null, false);
    }

    /** "Deals N damage to any target." */
    public static DealDamageEffect toAnyTarget(int damage) {
        return toAnyTarget(new Fixed(damage));
    }

    /** "Deals [amount] damage to any target." */
    public static DealDamageEffect toAnyTarget(DynamicAmount damage) {
        return of(damage, Recipient.ANY_TARGET);
    }

    /**
     * Damage to any target, aimed at target group {@code targetGroup}'s chosen target instead of the entry's single
     * target (Goblin Barrage's kicked target; for cards without {@code target(...)} declarations the index addresses
     * the flat additional-target list).
     */
    public static DealDamageEffect toAnyTargetInGroup(int damage, int targetGroup) {
        return toAnyTargetInGroup(new Fixed(damage), targetGroup);
    }

    /** {@link #toAnyTargetInGroup(int, int)} of an evaluated amount. */
    public static DealDamageEffect toAnyTargetInGroup(DynamicAmount damage, int targetGroup) {
        if (targetGroup < 0) {
            throw new IllegalArgumentException("A target group is 0 or more: " + targetGroup);
        }
        return toAnyTarget(damage).withTargetGroup(targetGroup);
    }

    /** Damage to any target other than the effect's source permanent. */
    public static DealDamageEffect toAnyOtherTarget(DynamicAmount damage) {
        return toAnyTarget(damage).withTargetGroup(ANY_OTHER_TARGET);
    }

    /**
     * Damage to the permanent or player the trigger event names ("deals 2 damage to that permanent or player", Ghyrson
     * Starn), the entry's non-targeting target (CR 115.10a): the ability targets nothing.
     */
    public static DealDamageEffect toEventObject(DynamicAmount damage) {
        return toAnyTarget(damage).withTargetGroup(EVENT_OBJECT);
    }

    /**
     * Damage to "any target of an opponent's choice" (Karplusan Minotaur): as the ability goes on the stack its
     * controller chooses one of their opponents, who chooses the target.
     */
    public static DealDamageEffect toAnyTargetOfAnOpponentsChoice(int damage) {
        return toAnyTarget(damage).withTargetGroup(OF_AN_OPPONENTS_CHOICE);
    }

    /**
     * Damage to "any target chosen at random" (Goblin Test Pilot): the target is picked among the legal targets as the
     * ability is activated or put on the stack (CR 601.2c, 603.3d), and checked again on resolution (CR 608.2b).
     */
    public static DealDamageEffect toAnyTargetChosenAtRandom(DynamicAmount damage) {
        return toAnyTarget(damage).withTargetGroup(CHOSEN_AT_RANDOM);
    }

    /** "Deals N damage to target creature." */
    public static DealDamageEffect toTargetCreature(int damage) {
        return toTargetCreature(new Fixed(damage));
    }

    /** "Deals [amount] damage to target creature." */
    public static DealDamageEffect toTargetCreature(DynamicAmount damage) {
        return of(damage, Recipient.CREATURE);
    }

    /** "Deals N damage to target [restricted] creature"; a {@code null} filter is plain "target creature". */
    public static DealDamageEffect toTargetCreature(int damage, PermanentPredicate targetFilter) {
        return toTargetCreature(new Fixed(damage), targetFilter);
    }

    /** "Deals [amount] damage to target [restricted] creature"; a {@code null} filter is plain "target creature". */
    public static DealDamageEffect toTargetCreature(DynamicAmount damage, PermanentPredicate targetFilter) {
        return toTargetCreature(damage).withTargetFilter(targetFilter);
    }

    /** {@code damage} to the one permanent the non-target {@code subject} names. */
    public static DealDamageEffect to(PermanentSubject subject, int damage) {
        return to(subject, new Fixed(damage));
    }

    /** {@code damage} to the one permanent the non-target {@code subject} names. */
    public static DealDamageEffect to(PermanentSubject subject, DynamicAmount damage) {
        return new DealDamageEffect(damage, Recipient.CREATURE, subject, null, PlayerRelation.ANY, -1,
                DamageSource.ABILITY_SOURCE, false, null, false, false, false, ExileIfItWouldDie.NONE, false, null, false);
    }

    /** "Deals N damage to target creature or planeswalker." */
    public static DealDamageEffect toTargetCreatureOrPlaneswalker(int damage) {
        return toTargetCreatureOrPlaneswalker(new Fixed(damage));
    }

    /** "Deals [amount] damage to target creature or planeswalker." */
    public static DealDamageEffect toTargetCreatureOrPlaneswalker(DynamicAmount damage) {
        return of(damage, Recipient.CREATURE_OR_PLANESWALKER);
    }

    /** "Deals N damage to target [restricted] creature or planeswalker"; {@code null} for no restriction. */
    public static DealDamageEffect toTargetCreatureOrPlaneswalker(int damage, PermanentPredicate targetFilter) {
        return toTargetCreatureOrPlaneswalker(new Fixed(damage), targetFilter);
    }

    /** "Deals [amount] damage to target [restricted] creature or planeswalker"; {@code null} for no restriction. */
    public static DealDamageEffect toTargetCreatureOrPlaneswalker(DynamicAmount damage,
                                                                  PermanentPredicate targetFilter) {
        return toTargetCreatureOrPlaneswalker(damage).withTargetFilter(targetFilter);
    }

    /** "Deals N damage to target player or planeswalker." */
    public static DealDamageEffect toTargetPlayerOrPlaneswalker(int damage) {
        return toTargetPlayerOrPlaneswalker(new Fixed(damage));
    }

    /** "Deals [amount] damage to target player or planeswalker." */
    public static DealDamageEffect toTargetPlayerOrPlaneswalker(DynamicAmount damage) {
        return of(damage, Recipient.PLAYER_OR_PLANESWALKER);
    }

    /** "Deals N damage to target opponent or planeswalker." */
    public static DealDamageEffect toTargetOpponentOrPlaneswalker(int damage) {
        return toTargetOpponentOrPlaneswalker(new Fixed(damage));
    }

    /** "Deals [amount] damage to target opponent or planeswalker." */
    public static DealDamageEffect toTargetOpponentOrPlaneswalker(DynamicAmount damage) {
        return new DealDamageEffect(damage, Recipient.PLAYER_OR_PLANESWALKER, Subjects.target(), null,
                PlayerRelation.OPPONENT, -1, DamageSource.ABILITY_SOURCE, false, null, false, false, false,
                ExileIfItWouldDie.NONE, false, null, false);
    }

    /** "Deals N damage to target permanent" (one that can be dealt damage, CR 120.1). */
    public static DealDamageEffect toTargetPermanent(int damage) {
        return toTargetPermanent(new Fixed(damage));
    }

    /** "Deals [amount] damage to target permanent" (one that can be dealt damage, CR 120.1). */
    public static DealDamageEffect toTargetPermanent(DynamicAmount damage) {
        return of(damage, Recipient.PERMANENT);
    }

    /**
     * "Deals N damage to [players]": each opponent, you, that player, target player, each player, "that creature's
     * controller" ({@code controllerOf(target())}).
     */
    public static DealDamageEffect to(PlayerSubject players, int damage) {
        return to(players, new Fixed(damage));
    }

    /**
     * "Deals [amount] damage to [players]". The amount is evaluated for each player with that player as the
     * controller for {@link Subjects#eachPlayer()}, with the defending player as the target for
     * {@link Subjects#defendingPlayer()}, else in the resolving entry's own context; every player's amount is fixed
     * before any damage is dealt (CR 608.2h).
     */
    public static DealDamageEffect to(PlayerSubject players, DynamicAmount damage) {
        return ofPlayers(damage, Recipient.PLAYERS, players, false);
    }

    /**
     * "Deals damage to each opponent equal to the number of cards in that player's hand": {@code damage} is evaluated
     * for each player {@code players} names, with that player as the controller (a {@code CONTROLLER} scope counts
     * that player's objects), every amount fixed before any damage (CR 608.2h).
     */
    public static DealDamageEffect eachEvaluatedFor(DynamicAmount damage, PlayerSubject players) {
        return ofPlayers(damage, Recipient.PLAYERS, players, true);
    }

    /**
     * "Deals N damage to that player or a planeswalker that player controls": the player {@code players} names, or a
     * planeswalker they control chosen as it resolves without targeting.
     */
    public static DealDamageEffect toPlayerOrTheirPlaneswalker(int damage, PlayerSubject players) {
        return toPlayerOrTheirPlaneswalker(new Fixed(damage), players);
    }

    /**
     * {@link #toPlayerOrTheirPlaneswalker(int, PlayerSubject)} of an evaluated amount ("choose an opponent at random.
     * It deals damage equal to that spell's mana value to that player or a planeswalker that player controls":
     * {@code playersThisWay(CHOOSE)} after a {@code ChoosePlayerAtRandomEffect}, Vial Smasher the Fierce).
     */
    public static DealDamageEffect toPlayerOrTheirPlaneswalker(DynamicAmount damage, PlayerSubject players) {
        return ofPlayers(damage, Recipient.PLAYER_OR_THEIR_PLANESWALKER, players, false);
    }

    /**
     * "Deals N damage to each [permanent of a set]": each permanent the non-target {@code set} names, found once as the
     * effect is applied (CR 608.2h), in one damage event: "each creature target player controls"
     * ({@code controlledBy(targetPlayer(), creature)}, which declares the player target), "each attacking creature"
     * ({@code controlledBy(eachPlayer(), attacking)}), "each creature defending player controls", "each of those
     * creatures" ({@code triggeringAttackers()}), "each other creature that shares a color with it"
     * ({@code otherCreaturesSharing(target(), COLOR)}, beside the target's own damage).
     */
    public static DealDamageEffect toEach(PermanentSubject set, int damage) {
        return toEach(set, new Fixed(damage));
    }

    /** {@link #toEach(PermanentSubject, int)} of an amount evaluated once as it resolves. */
    public static DealDamageEffect toEach(PermanentSubject set, DynamicAmount damage) {
        return new DealDamageEffect(damage, Recipient.EACH_PERMANENT, Objects.requireNonNull(set, "set"), null,
                PlayerRelation.ANY, -1, DamageSource.ABILITY_SOURCE, false, null, false, false, false,
                ExileIfItWouldDie.NONE, false, null, false);
    }

    /** "Deals N damage to each creature" (every player's). */
    public static DealDamageEffect toEachCreature(int damage) {
        return toEachCreature(new Fixed(damage));
    }

    /** {@link #toEachCreature(int)} of an amount evaluated once as it resolves. */
    public static DealDamageEffect toEachCreature(DynamicAmount damage) {
        return toEach(Subjects.allCreatures(), damage);
    }

    /**
     * "Deals N damage to each [creature matching {@code filter}]": "each creature without flying", "each other
     * creature", "each creature your opponents control"; the filter is judged with the effect's source as its source.
     */
    public static DealDamageEffect toEachCreature(int damage, PermanentPredicate filter) {
        return toEachCreature(new Fixed(damage), filter);
    }

    /** {@link #toEachCreature(int, PermanentPredicate)} of an amount evaluated once as it resolves. */
    public static DealDamageEffect toEachCreature(DynamicAmount damage, PermanentPredicate filter) {
        return toEach(Subjects.controlledBy(Subjects.eachPlayer(), new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), Objects.requireNonNull(filter, "filter")))), damage);
    }

    /** "Deals N damage to each creature and each planeswalker". */
    public static DealDamageEffect toEachCreatureAndPlaneswalker(int damage) {
        return toEachCreatureAndPlaneswalker(new Fixed(damage));
    }

    /** {@link #toEachCreatureAndPlaneswalker(int)} of an amount evaluated once as it resolves. */
    public static DealDamageEffect toEachCreatureAndPlaneswalker(DynamicAmount damage) {
        return toEach(Subjects.controlledBy(Subjects.eachPlayer(), CREATURE_OR_PLANESWALKER), damage);
    }

    /**
     * "Deals N damage to each creature and planeswalker [matching {@code filter}]": the filter narrows both ("each
     * creature and planeswalker you don't control", "each non-Bolas planeswalker").
     */
    public static DealDamageEffect toEachCreatureAndPlaneswalker(int damage, PermanentPredicate filter) {
        return toEachCreatureAndPlaneswalker(new Fixed(damage), filter);
    }

    /** {@link #toEachCreatureAndPlaneswalker(int, PermanentPredicate)} of an amount evaluated once as it resolves. */
    public static DealDamageEffect toEachCreatureAndPlaneswalker(DynamicAmount damage, PermanentPredicate filter) {
        return toEach(Subjects.controlledBy(Subjects.eachPlayer(), new PermanentAllOfPredicate(List.of(
                CREATURE_OR_PLANESWALKER, Objects.requireNonNull(filter, "filter")))), damage);
    }

    /**
     * "Deals 2 damage to each creature for each Aura attached to that creature": {@code damage} is evaluated for each
     * permanent of the non-target {@code set} with that permanent as the amount's source, every amount fixed before
     * any damage (CR 608.2h), all in one damage event.
     */
    public static DealDamageEffect eachEvaluatedFor(DynamicAmount damage, PermanentSubject set) {
        DealDamageEffect each = toEach(set, damage);
        return new DealDamageEffect(each.damage, each.recipient, each.subject, null, PlayerRelation.ANY, -1,
                DamageSource.ABILITY_SOURCE, false, null, false, false, false, ExileIfItWouldDie.NONE, false, null,
                true);
    }

    /**
     * Damage to each permanent of a set only: the players {@code alsoDealt} names are dealt the same damage in the
     * same damage event ("deals 3 damage to each creature and each player": {@code andTo(Subjects.eachPlayer())}).
     */
    public DealDamageEffect andTo(PlayerSubject alsoDealt) {
        if (recipient != Recipient.EACH_PERMANENT) {
            throw new IllegalArgumentException("Only damage to each permanent of a set is also dealt to players");
        }
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable, cantRegenerate,
                preventRegenerationWithoutDamage, exile, recordDamageDealt,
                Objects.requireNonNull(alsoDealt, "alsoDealt"), amountForEachRecipient);
    }

    /**
     * "That player or that planeswalker's controller": the player target another effect or the card declares, or the
     * controller of the targeted planeswalker.
     */
    private static final PlayerSubject TARGETED_PLAYER = Subjects.allOf(Subjects.targetPlayerDeclaredElsewhere(),
            Subjects.controllerOf(Subjects.target()));

    /**
     * "... to target player [or planeswalker] and N damage to each creature that player [or that planeswalker's
     * controller] controls": the player (or the controller of the planeswalker) is the target the card or another
     * effect declares (Chandra's Fury, Simoon, Radiating Lightning).
     */
    public static DealDamageEffect toEachCreatureOfTargetedPlayer(int damage) {
        return toEachControlledByTargetedPlayer(new Fixed(damage), new PermanentIsCreaturePredicate());
    }

    /** {@code damage} to each permanent matching {@code filter} that the targeted player, or the targeted planeswalker's
     * controller, controls (Chandra, Bold Pyromancer's "each creature and planeswalker they control"). */
    public static DealDamageEffect toEachControlledByTargetedPlayer(int damage, PermanentPredicate filter) {
        return toEachControlledByTargetedPlayer(new Fixed(damage), filter);
    }

    /** {@link #toEachControlledByTargetedPlayer(int, PermanentPredicate)} of an evaluated amount. */
    public static DealDamageEffect toEachControlledByTargetedPlayer(DynamicAmount damage, PermanentPredicate filter) {
        return toEach(Subjects.controlledBy(TARGETED_PLAYER, filter), damage);
    }

    /**
     * "... to target player or planeswalker and each creature that player or that planeswalker's controller controls"
     * (Flame Wave): {@code damage} to each such creature but the targeted planeswalker itself, which the target's own
     * damage effect beside this one deals to, so an animated planeswalker is dealt the damage once.
     */
    public static DealDamageEffect toCreaturesOfTargetedPlayer(DynamicAmount damage) {
        return toEachControlledByTargetedPlayer(damage, new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentNotPredicate(new PermanentIsTargetCardPredicate()))));
    }

    /** "Deals N damage to each of [up to] N targets" (any target, CR 115.4), the full amount to each. */
    public static DealDamageEffect toEachTarget(int damage) {
        return toEachTarget(new Fixed(damage));
    }

    /** {@link #toEachTarget(int)} of an amount evaluated once as it resolves. */
    public static DealDamageEffect toEachTarget(DynamicAmount damage) {
        return of(damage, Recipient.EACH_TARGET);
    }

    /** "Deals [amount] damage to each of [up to] N target creatures." */
    public static DealDamageEffect toEachTargetCreature(DynamicAmount damage) {
        return toEachTarget(damage).withTargetFilter(new PermanentIsCreaturePredicate());
    }

    /** "Deals [amount] damage to each of [up to] N target creatures and/or planeswalkers." */
    public static DealDamageEffect toEachTargetCreatureOrPlaneswalker(DynamicAmount damage) {
        return toEachTarget(damage).withTargetFilter(
                TargetPredicates.creatureOrPlaneswalker().permanentRestriction().orElseThrow());
    }

    private static DealDamageEffect ofPlayers(DynamicAmount damage, Recipient recipient, PlayerSubject players,
                                              boolean amountForEachRecipient) {
        return new DealDamageEffect(damage, recipient, null, null, PlayerRelation.ANY, -1,
                DamageSource.ABILITY_SOURCE, false, null, false, false, false, ExileIfItWouldDie.NONE, false,
                Objects.requireNonNull(players, "players"), amountForEachRecipient);
    }

    /**
     * Player damage only: the permanent that dealt the damage the trigger event was about deals this damage
     * ({@link DamageSource#EVENT_DAMAGE_SOURCE}): "that Archer deals that much damage to that creature's controller"
     * (Greatbow Doyen), "it deals that much damage to the chosen player" (Saskia the Unyielding).
     */
    public DealDamageEffect byEventDamageSource() {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                DamageSource.EVENT_DAMAGE_SOURCE, alwaysUnpreventable, unpreventableWhen,
                cantBeRedirectedWhenUnpreventable, cantRegenerate, preventRegenerationWithoutDamage, exile,
                recordDamageDealt, players, amountForEachRecipient);
    }

    /** The same effect dealing {@code newDamage} instead (an amount fixed as it triggers). */
    public DealDamageEffect withDamage(DynamicAmount newDamage) {
        return new DealDamageEffect(newDamage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable, cantRegenerate,
                preventRegenerationWithoutDamage, exile, recordDamageDealt, players, amountForEachRecipient);
    }

    private DealDamageEffect withTargetGroup(int newTargetGroup) {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, newTargetGroup,
                damageSource, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable, cantRegenerate,
                preventRegenerationWithoutDamage, exile, recordDamageDealt, players, amountForEachRecipient);
    }

    private DealDamageEffect withTargetFilter(PermanentPredicate newTargetFilter) {
        return new DealDamageEffect(damage, recipient, subject, newTargetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable, cantRegenerate,
                preventRegenerationWithoutDamage, exile, recordDamageDealt, players, amountForEachRecipient);
    }

    /**
     * The permanent the trigger event was about (the creature that entered, attacked, was dealt damage, died, …) deals
     * the damage, as it is or as it last existed on the battlefield (CR 608.2h: "it deals", "have it deal"), so the
     * damage has its characteristics: its lifelink, infect and deathtouch, the colours protection reads.
     */
    public DealDamageEffect byTriggeringPermanent() {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                DamageSource.TRIGGERING_PERMANENT, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable,
                cantRegenerate, preventRegenerationWithoutDamage, exile, recordDamageDealt, players, amountForEachRecipient);
    }

    /** "The damage can't be prevented" (CR 615.12), for this one damage event. */
    public DealDamageEffect unpreventable() {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, true, unpreventableWhen, cantBeRedirectedWhenUnpreventable, cantRegenerate,
                preventRegenerationWithoutDamage, exile, recordDamageDealt, players, amountForEachRecipient);
    }

    /** "If [condition], the damage can't be prevented" (Banefire's X of 5 or more, Urza's Rage kicked). */
    public DealDamageEffect unpreventableWhen(Condition condition) {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, condition, cantBeRedirectedWhenUnpreventable, cantRegenerate,
                preventRegenerationWithoutDamage, exile, recordDamageDealt, players, amountForEachRecipient);
    }

    /** "If [condition], the damage can't be prevented or redirected" (Lava Burst). */
    public DealDamageEffect unpreventableAndUnredirectableWhen(Condition condition) {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, condition, true, cantRegenerate, preventRegenerationWithoutDamage, exile,
                recordDamageDealt, players, amountForEachRecipient);
    }

    /** "A creature dealt damage this way can't be regenerated this turn" (Incinerate, CR 701.19c). */
    public DealDamageEffect cantBeRegenerated() {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable, true,
                preventRegenerationWithoutDamage, exile, recordDamageDealt, players, amountForEachRecipient);
    }

    /**
     * "If it's a creature, it can't be regenerated this turn": the targeted creature can't be regenerated whether or
     * not it is dealt damage (Disintegrate, Carbonize).
     */
    public DealDamageEffect withUnconditionalRegenerationPrevention() {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable, true, true,
                exile == ExileIfItWouldDie.DAMAGED_CREATURE ? ExileIfItWouldDie.TARGET : exile, recordDamageDealt,
                players, amountForEachRecipient);
    }

    /** "If a creature dealt damage this way would die this turn, exile it instead" (Red Sun's Zenith). */
    public DealDamageEffect exilingDamagedCreaturesThatWouldDie() {
        return withExile(preventRegenerationWithoutDamage ? ExileIfItWouldDie.TARGET : ExileIfItWouldDie.DAMAGED_CREATURE);
    }

    /** "If that creature or planeswalker would die this turn, exile it instead": the target, damaged or not. */
    public DealDamageEffect exilingIfItWouldDie() {
        return withExile(ExileIfItWouldDie.TARGET);
    }

    /** "If a permanent dealt damage this way would die this turn, exile it instead" (Torch the Tower). */
    public DealDamageEffect exilingPermanentsDealtDamage() {
        return withExile(ExileIfItWouldDie.DAMAGED_PERMANENT);
    }

    private DealDamageEffect withExile(ExileIfItWouldDie newExile) {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable, cantRegenerate,
                preventRegenerationWithoutDamage, newExile, recordDamageDealt, players, amountForEachRecipient);
    }

    /** The damage actually dealt is the entry's event value for the following effects (Corrupt's life gain). */
    public DealDamageEffect recordingDamageDealt() {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable, cantRegenerate,
                preventRegenerationWithoutDamage, exile, true, players, amountForEachRecipient);
    }

    /** Whether the any-target damage goes to the entry's non-targeting event object (Ghyrson Starn). */
    public boolean dealtToEventObject() {
        return recipient == Recipient.ANY_TARGET && targetGroup == EVENT_OBJECT;
    }

    @Override
    public boolean targetChosenByAnOpponent() {
        return recipient == Recipient.ANY_TARGET && targetGroup == OF_AN_OPPONENTS_CHOICE;
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return playerRelation;
    }

    /** Target filter for an any-target effect that excludes its source permanent. */
    public static TargetFilter anyOtherTargetFilter() {
        return new AnyTargetPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        TargetPredicates.anyTarget().permanentRestriction().orElseThrow(),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be another target");
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (recipient) {
            case ANY_TARGET -> switch (targetGroup) {
                case EVENT_OBJECT -> TargetSpec.NONE;
                case CHOSEN_AT_RANDOM -> TargetSpec.harmful(TargetPredicates.anyTarget()).atRandom();
                case ANY_OTHER_TARGET -> TargetSpec.harmful(TargetPredicates.anyTarget(),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()));
                default -> TargetSpec.harmful(TargetPredicates.anyTarget());
            };
            case CREATURE -> subject.declaresTarget()
                    ? TargetSpec.harmful(TargetPredicates.creature(), targetFilter)
                    : TargetSpec.NONE;
            case CREATURE_OR_PLANESWALKER -> TargetSpec.harmful(TargetPredicates.creatureOrPlaneswalker(), targetFilter);
            case PLAYER_OR_PLANESWALKER -> TargetSpec.harmful(TargetPredicates.playerOrPlaneswalker());
            case PERMANENT -> TargetSpec.harmful(TargetPredicates.permanent());
            case PLAYERS, PLAYER_OR_THEIR_PLANESWALKER -> playersTargetSpec();
            case EACH_PERMANENT -> subject.declaresPlayerTarget()
                    ? TargetSpec.harmful(TargetPredicates.player())
                    : new TargetSpec(null, true, null, false, 1);
            case EACH_TARGET -> targetFilter == null
                    ? TargetSpec.harmful(TargetPredicates.anyTarget())
                    : TargetSpec.harmful(TargetPredicates.permanents(targetFilter));
        };
    }

    /**
     * A declared {@code targetPlayer()} targets a player. "That creature's controller" ({@code controllerOf(target())}),
     * "that spell's controller" ({@code controllerOf(targetSpell())}) and "the owner of target [permanent]"
     * ({@code ownerOf(target())}, Kaya, Ghost Haunter's emblem) declare the permanent or spell they read, so the target
     * is chosen even when no companion effect declares it (Fodder Launch, Ionize). Benign: the damage lands on a
     * player, not the permanent or spell; {@code DamageTargetValidators} enforces "must be a player" for the player
     * target, which the player spec cannot express.
     */
    private TargetSpec playersTargetSpec() {
        if (players.declaresTarget()) {
            TargetSpec spec = TargetSpec.benign(TargetPredicates.player());
            return players.targetChosenAtRandom() ? spec.atRandom() : spec;
        }
        if (players instanceof PlayerSubject.OwnerOf ownerOf && ownerOf.permanent() instanceof PermanentSubject.Target) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        if (players instanceof PlayerSubject.ControllerOf controllerOf) {
            if (controllerOf.permanent() instanceof PermanentSubject.Target) {
                return TargetSpec.benign(TargetPredicates.permanent());
            }
            if (controllerOf.permanent() instanceof PermanentSubject.TargetSpell) {
                return TargetSpec.benign(TargetPredicates.spellOnStack());
            }
        }
        return TargetSpec.NONE;
    }

    @Override
    public TargetFilter triggeredTargetFilter() {
        return recipient == Recipient.ANY_TARGET && targetGroup == ANY_OTHER_TARGET ? anyOtherTargetFilter() : null;
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    /**
     * A permanent damaging itself is no damage it can aim at a creature, so {@link Subjects#source()} answers no; nor
     * is player damage. Damage to several permanents or targets at once answers no to both: {@link DamageDealingEffect}
     * describes damage to one target, and its readers score it so.
     */
    @Override
    public boolean canDamageCreatures() {
        return recipient != Recipient.PLAYER_OR_PLANESWALKER && !recipient.namesPlayers()
                && !recipient.dealsToSeveral() && !(subject instanceof PermanentSubject.Source);
    }

    @Override
    public boolean canDamagePlayers() {
        return recipient == Recipient.ANY_TARGET || recipient == Recipient.PLAYER_OR_PLANESWALKER
                || recipient.namesPlayers();
    }

    /**
     * Damage to each permanent a set over every player's permanents names ("each creature", "each creature without
     * flying and each player") sweeps the board.
     */
    @Override
    public boolean sweepsBoard() {
        return recipient == Recipient.EACH_PERMANENT && subject instanceof PermanentSubject.ControlledBy set
                && set.players() instanceof PlayerSubject.Each each && each.relation() == PlayerSubject.Relation.ALL;
    }

    /** "Deals N damage to you" (a pain land's mana ability, Volcano Hellion). */
    @Override
    public boolean damagesController() {
        return players instanceof PlayerSubject.You;
    }
}

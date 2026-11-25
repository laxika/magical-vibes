package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

/**
 * One source deals {@code damage} to one recipient: "deals N damage to any target", "to target creature", "to target
 * creature or planeswalker", "to target player or planeswalker", "to target permanent", or to the one permanent a
 * non-target {@code subject} names ("to itself", "to it", "to enchanted creature"). The amount is a
 * {@link DynamicAmount} evaluated as it resolves, against the dealing permanent as it is or last existed.
 *
 * <p>Build it with the factories, which read like the wording: {@link #toAnyTarget}, {@link #toTargetCreature},
 * {@link #toTargetCreatureOrPlaneswalker}, {@link #toTargetPlayerOrPlaneswalker},
 * {@link #toTargetOpponentOrPlaneswalker}, {@link #toTargetPermanent}, {@link #to(PermanentSubject, int)}, the
 * any-target markers ({@link #toAnyOtherTarget}, {@link #toEventObject}, {@link #toAnyTargetOfAnOpponentsChoice},
 * {@link #toAnyTargetChosenAtRandom}, {@link #toAnyTargetInGroup}), and the riders chained after one
 * ({@link #byTriggeringPermanent}, {@link #unpreventable}, {@link #unpreventableWhen}, {@link #cantBeRegenerated},
 * {@link #withUnconditionalRegenerationPrevention}, {@link #exilingDamagedCreaturesThatWouldDie},
 * {@link #exilingIfItWouldDie}, {@link #exilingPermanentsDealtDamage}, {@link #recordingDamageDealt}).
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
 *                                          non-target creature subject
 * @param targetFilter                      an extra restriction on a targeted creature or creature-or-planeswalker
 *                                          ("target creature an opponent controls"); {@code null} for none
 * @param playerRelation                    {@link PlayerRelation#OPPONENT} for "target opponent or planeswalker";
 *                                          {@link PlayerRelation#ANY} otherwise. The narrowing isn't expressible in
 *                                          the spec: {@code DamageTargetValidators} enforces it
 * @param targetGroup                       any target only: {@code -1} the entry's target, {@code >= 0} that target
 *                                          group's chosen target, the negative markers of the any-target factories
 * @param damageSource                      what deals the damage: the ability's own source, or the permanent the
 *                                          trigger event was about ("it deals", CR 608.2h)
 * @param alwaysUnpreventable               "the damage can't be prevented" (CR 615.12), for this one damage event
 * @param unpreventableWhen                 when met as it resolves, this damage can't be prevented (CR 615.12):
 *                                          Banefire's {@code SpellXAtLeast(5)}; {@code null} for none
 * @param cantBeRedirectedWhenUnpreventable damage made unpreventable by {@code unpreventableWhen} also can't be
 *                                          redirected (Lava Burst)
 * @param cantRegenerate                    a creature dealt damage this way can't be regenerated this turn
 *                                          (Incinerate); any target only
 * @param preventRegenerationWithoutDamage  the targeted creature can't be regenerated even if no damage is dealt
 *                                          (Disintegrate); any target only
 * @param exile                             which permanent is exiled instead if it would die this turn
 * @param recordDamageDealt                 any target only: the damage actually dealt is the entry's event value
 *                                          for later effects (Corrupt)
 */
public record DealDamageEffect(DynamicAmount damage, Recipient recipient, PermanentSubject subject,
                               PermanentPredicate targetFilter, PlayerRelation playerRelation, int targetGroup,
                               DamageSource damageSource, boolean alwaysUnpreventable, Condition unpreventableWhen,
                               boolean cantBeRedirectedWhenUnpreventable, boolean cantRegenerate,
                               boolean preventRegenerationWithoutDamage, ExileIfItWouldDie exile,
                               boolean recordDamageDealt)
        implements DamageDealingEffect {

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
        PERMANENT
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

    private static final int ANY_OTHER_TARGET = -2;
    private static final int EVENT_OBJECT = -3;
    private static final int OF_AN_OPPONENTS_CHOICE = -4;
    private static final int CHOSEN_AT_RANDOM = -5;

    public DealDamageEffect {
        subject = subject == null ? Subjects.target() : subject;
        playerRelation = playerRelation == null ? PlayerRelation.ANY : playerRelation;
        damageSource = damageSource == null ? DamageSource.ABILITY_SOURCE : damageSource;
        exile = exile == null ? ExileIfItWouldDie.NONE : exile;
        if (damageSource == DamageSource.EVENT_DAMAGE_SOURCE) {
            throw new IllegalArgumentException("A damage effect is dealt by its source or its triggering permanent");
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
                DamageSource.ABILITY_SOURCE, false, null, false, false, false, ExileIfItWouldDie.NONE, false);
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
                DamageSource.ABILITY_SOURCE, false, null, false, false, false, ExileIfItWouldDie.NONE, false);
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
                ExileIfItWouldDie.NONE, false);
    }

    /** "Deals N damage to target permanent" (one that can be dealt damage, CR 120.1). */
    public static DealDamageEffect toTargetPermanent(int damage) {
        return toTargetPermanent(new Fixed(damage));
    }

    /** "Deals [amount] damage to target permanent" (one that can be dealt damage, CR 120.1). */
    public static DealDamageEffect toTargetPermanent(DynamicAmount damage) {
        return of(damage, Recipient.PERMANENT);
    }

    /** The same effect dealing {@code newDamage} instead (an amount fixed as it triggers). */
    public DealDamageEffect withDamage(DynamicAmount newDamage) {
        return new DealDamageEffect(newDamage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable, cantRegenerate,
                preventRegenerationWithoutDamage, exile, recordDamageDealt);
    }

    private DealDamageEffect withTargetGroup(int newTargetGroup) {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, newTargetGroup,
                damageSource, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable, cantRegenerate,
                preventRegenerationWithoutDamage, exile, recordDamageDealt);
    }

    private DealDamageEffect withTargetFilter(PermanentPredicate newTargetFilter) {
        return new DealDamageEffect(damage, recipient, subject, newTargetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable, cantRegenerate,
                preventRegenerationWithoutDamage, exile, recordDamageDealt);
    }

    /**
     * The permanent the trigger event was about (the creature that entered, attacked, was dealt damage, died, …) deals
     * the damage, as it is or as it last existed on the battlefield (CR 608.2h: "it deals", "have it deal"), so the
     * damage has its characteristics: its lifelink, infect and deathtouch, the colours protection reads.
     */
    public DealDamageEffect byTriggeringPermanent() {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                DamageSource.TRIGGERING_PERMANENT, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable,
                cantRegenerate, preventRegenerationWithoutDamage, exile, recordDamageDealt);
    }

    /** "The damage can't be prevented" (CR 615.12), for this one damage event. */
    public DealDamageEffect unpreventable() {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, true, unpreventableWhen, cantBeRedirectedWhenUnpreventable, cantRegenerate,
                preventRegenerationWithoutDamage, exile, recordDamageDealt);
    }

    /** "If [condition], the damage can't be prevented" (Banefire's X of 5 or more, Urza's Rage kicked). */
    public DealDamageEffect unpreventableWhen(Condition condition) {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, condition, cantBeRedirectedWhenUnpreventable, cantRegenerate,
                preventRegenerationWithoutDamage, exile, recordDamageDealt);
    }

    /** "If [condition], the damage can't be prevented or redirected" (Lava Burst). */
    public DealDamageEffect unpreventableAndUnredirectableWhen(Condition condition) {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, condition, true, cantRegenerate, preventRegenerationWithoutDamage, exile,
                recordDamageDealt);
    }

    /** "A creature dealt damage this way can't be regenerated this turn" (Incinerate, CR 701.19c). */
    public DealDamageEffect cantBeRegenerated() {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable, true,
                preventRegenerationWithoutDamage, exile, recordDamageDealt);
    }

    /**
     * "If it's a creature, it can't be regenerated this turn": the targeted creature can't be regenerated whether or
     * not it is dealt damage (Disintegrate, Carbonize).
     */
    public DealDamageEffect withUnconditionalRegenerationPrevention() {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable, true, true,
                exile == ExileIfItWouldDie.DAMAGED_CREATURE ? ExileIfItWouldDie.TARGET : exile, recordDamageDealt);
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
                preventRegenerationWithoutDamage, newExile, recordDamageDealt);
    }

    /** The damage actually dealt is the entry's event value for the following effects (Corrupt's life gain). */
    public DealDamageEffect recordingDamageDealt() {
        return new DealDamageEffect(damage, recipient, subject, targetFilter, playerRelation, targetGroup,
                damageSource, alwaysUnpreventable, unpreventableWhen, cantBeRedirectedWhenUnpreventable, cantRegenerate,
                preventRegenerationWithoutDamage, exile, true);
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
        };
    }

    @Override
    public TargetFilter triggeredTargetFilter() {
        return recipient == Recipient.ANY_TARGET && targetGroup == ANY_OTHER_TARGET ? anyOtherTargetFilter() : null;
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    /** A permanent damaging itself is no damage it can aim at a creature, so {@link Subjects#source()} answers no. */
    @Override
    public boolean canDamageCreatures() {
        return recipient != Recipient.PLAYER_OR_PLANESWALKER && !(subject instanceof PermanentSubject.Source);
    }

    @Override
    public boolean canDamagePlayers() {
        return recipient == Recipient.ANY_TARGET || recipient == Recipient.PLAYER_OR_PLANESWALKER;
    }
}

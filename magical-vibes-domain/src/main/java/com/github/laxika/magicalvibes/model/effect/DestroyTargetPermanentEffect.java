package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Destroys the permanents {@code subject} names: by default the targeted permanent ("destroy target creature"), or a
 * permanent named without targeting it: {@link Subjects#source()} ("destroy this Aura", Arachnus Web, Ice Cage, Chaos
 * Orb's "then destroy this artifact"), {@link Subjects#attached()} ("destroy enchanted creature", Yoke of the Damned,
 * Pooling Venom; "destroy that creature" of an Aura's leaves trigger, Parallax Dementia),
 * {@link Subjects#attachedAtTrigger()} (Venarian Gold), {@link Subjects#triggering()} ("destroy it", "destroy that
 * creature" for the permanent the trigger event was about: Suleiman's Legacy, Mortal Wound, Spinal Graft, Dwindle) or
 * {@code allOf(source(), target())} ("destroy this creature and target creature it's blocking", Wall of Vipers).
 * Several permanents are destroyed as one event, so each sees the others die with it. Optionally creates a creature
 * token for the target's controller (e.g. Beast Within, Pongify, Rapid Hybridization).
 *
 * <p>"Destroy all [permanents]" is a set subject ({@link #all}, {@link #allTargetPlayerControls}, {@link #sparing},
 * {@link #readingTarget}): the set is found once as the effect applies, its filter's amounts, the triggering spell's
 * mana value and "shares a creature type with" read then (CR 608.2h), and it sweeps the board
 * ({@link #sweepsBoard()}). "Destroy each of the targets" is {@link #eachTarget} over {@link Subjects#allTargets()}.
 * Every several-permanent destruction is a {@code StepKind.DESTROY} step that records its departures, so a later step
 * reads what it did through {@code AmountThisWay} ("for each creature that died this way"), {@code HappenedThisWay}
 * and {@code playersThisWay(StepKind.DESTROY)}; indestructible and regenerated permanents are not destroyed, and one a
 * replacement exiled instead of putting it into a graveyard was destroyed but didn't die.
 *
 * <p>Only a subject with a target part targets ({@link #targetSpec()} is then harmful, narrowed by
 * {@code targetFilter}); "creatures target player controls" declares the harmful player target; any other subject
 * declares no target, so protection, shroud and hexproof don't stop it and a permanent that is gone at resolution is
 * simply not destroyed.
 *
 * @param cannotBeRegenerated whether the destroyed permanents can't be regenerated
 * @param tokenForController  if non-null, creates this token for the destroyed permanent's controller (target only)
 * @param targetGroup         activated-ability target-group index, or {@code -1} for the primary target; the same group
 *                            {@code subject} reads when it is a {@code target(group)}
 * @param targetFilter        optional predicate narrowing the permanent target
 * @param tokenOnlyForSourceController whether the token is created only when the target was controlled by the effect's controller
 * @param tokenCount           number of tokens to create when {@code tokenForController} is non-null
 * @param targetChosenByActivePlayer whether an upkeep trigger has its target chosen by the active player
 * @param subject             which permanents are destroyed ({@code target()} or {@code target(targetGroup)} by default)
 * @param referencedTarget    the target a set's filter amount reads but the effect doesn't destroy for being it
 *                            ("power greater than target creature's power", Fell the Mighty), or {@code null}; the
 *                            effect declares it, so the spell is countered if it is illegal (CR 608.2b)
 */
public record DestroyTargetPermanentEffect(
        boolean cannotBeRegenerated,
        CreateTokenEffect tokenForController,
        int targetGroup,
        PermanentPredicate targetFilter,
        boolean tokenOnlyForSourceController,
        int tokenCount,
        boolean targetChosenByActivePlayer,
        PermanentSubject subject,
        TargetSpec referencedTarget
) implements RemovalEffect, BoardWipeEffect {

    public DestroyTargetPermanentEffect {
        if (subject == null) {
            subject = targetGroup >= 0 ? Subjects.target(targetGroup) : Subjects.target();
        } else if (subject instanceof PermanentSubject.Target target) {
            if (target.group() != null) {
                if (targetGroup >= 0 && targetGroup != target.group()) {
                    throw new IllegalArgumentException("targetGroup " + targetGroup + " and subject " + subject
                            + " name different target groups");
                }
                targetGroup = target.group();
            } else if (targetGroup >= 0) {
                subject = Subjects.target(targetGroup);
            }
        } else {
            if (targetGroup >= 0 || tokenForController != null || targetChosenByActivePlayer) {
                throw new IllegalArgumentException("A target group, a token for the target's controller and an "
                        + "active-player target choice need a target subject: " + subject);
            }
            if (targetFilter != null && !subject.declaresTarget()) {
                throw new IllegalArgumentException("A target filter needs a subject that targets: " + subject);
            }
        }
        if (referencedTarget != null && (subject.declaresTarget() || subject.declaresPlayerTarget())) {
            throw new IllegalArgumentException("A referenced target needs a subject that declares no target: "
                    + subject);
        }
    }

    public DestroyTargetPermanentEffect(boolean cannotBeRegenerated, CreateTokenEffect tokenForController,
                                        int targetGroup, PermanentPredicate targetFilter,
                                        boolean tokenOnlyForSourceController, int tokenCount,
                                        boolean targetChosenByActivePlayer, PermanentSubject subject) {
        this(cannotBeRegenerated, tokenForController, targetGroup, targetFilter, tokenOnlyForSourceController,
                tokenCount, targetChosenByActivePlayer, subject, null);
    }

    public DestroyTargetPermanentEffect(boolean cannotBeRegenerated, CreateTokenEffect tokenForController,
                                        int targetGroup, PermanentPredicate targetFilter,
                                        boolean tokenOnlyForSourceController, int tokenCount,
                                        boolean targetChosenByActivePlayer) {
        this(cannotBeRegenerated, tokenForController, targetGroup, targetFilter, tokenOnlyForSourceController,
                tokenCount, targetChosenByActivePlayer, null);
    }

    public DestroyTargetPermanentEffect() {
        this(false, null, -1, null, false, 1, false);
    }

    public DestroyTargetPermanentEffect(boolean cannotBeRegenerated) {
        this(cannotBeRegenerated, null, -1, null, false, 1, false);
    }

    public DestroyTargetPermanentEffect(boolean cannotBeRegenerated, CreateTokenEffect tokenForController) {
        this(cannotBeRegenerated, tokenForController, -1, null, false, 1, false);
    }

    public DestroyTargetPermanentEffect(boolean cannotBeRegenerated, CreateTokenEffect tokenForController,
                                        boolean tokenOnlyForSourceController) {
        this(cannotBeRegenerated, tokenForController, -1, null, tokenOnlyForSourceController, 1, false);
    }

    public DestroyTargetPermanentEffect(boolean cannotBeRegenerated, CreateTokenEffect tokenForController,
                                        int tokenCount, boolean tokenOnlyForSourceController) {
        this(cannotBeRegenerated, tokenForController, -1, null, tokenOnlyForSourceController, tokenCount, false);
    }

    public DestroyTargetPermanentEffect(boolean cannotBeRegenerated, CreateTokenEffect tokenForController,
                                        int targetGroup, PermanentPredicate targetFilter) {
        this(cannotBeRegenerated, tokenForController, targetGroup, targetFilter, false, 1, false);
    }

    public DestroyTargetPermanentEffect(boolean cannotBeRegenerated, CreateTokenEffect tokenForController,
                                        int targetGroup, PermanentPredicate targetFilter,
                                        boolean tokenOnlyForSourceController, int tokenCount) {
        this(cannotBeRegenerated, tokenForController, targetGroup, targetFilter,
                tokenOnlyForSourceController, tokenCount, false);
    }

    public DestroyTargetPermanentEffect(PermanentPredicate targetFilter) {
        this(false, null, -1, targetFilter, false, 1, false);
    }

    /** Destroys what {@code subject} names; regeneration still applies. */
    public DestroyTargetPermanentEffect(PermanentSubject subject) {
        this(subject, false);
    }

    /** Destroys what {@code subject} names; {@code cannotBeRegenerated} for "It can't be regenerated." */
    public DestroyTargetPermanentEffect(PermanentSubject subject, boolean cannotBeRegenerated) {
        this(cannotBeRegenerated, null, -1, null, false, 1, false, subject);
    }

    /** Destroys what {@code subject} names, its target part narrowed by {@code targetFilter}. */
    public DestroyTargetPermanentEffect(PermanentSubject subject, PermanentPredicate targetFilter) {
        this(false, null, -1, targetFilter, false, 1, false, subject);
    }

    /** "Destroy all [filter]": every matching permanent on every battlefield, found once as the effect applies. */
    public static DestroyTargetPermanentEffect all(PermanentPredicate filter) {
        return all(filter, false);
    }

    /** "Destroy all [filter]. They can't be regenerated." with {@code cannotBeRegenerated}. */
    public static DestroyTargetPermanentEffect all(PermanentPredicate filter, boolean cannotBeRegenerated) {
        return new DestroyTargetPermanentEffect(Subjects.controlledBy(Subjects.eachPlayer(), filter),
                cannotBeRegenerated);
    }

    /** "Destroy all [filter] target player controls": declares the harmful player target. */
    public static DestroyTargetPermanentEffect allTargetPlayerControls(PermanentPredicate filter) {
        return new DestroyTargetPermanentEffect(Subjects.controlledBy(Subjects.targetPlayer(), filter));
    }

    /**
     * "Destroy the rest", "destroy all other [filter]": every matching permanent but those {@code spared} names, an
     * earlier step's result ("destroy all other creatures" after creating tokens, Martial Coup:
     * {@link Subjects#createdThisResolution()}; "destroy the rest" after a choice, Duneblast).
     */
    public static DestroyTargetPermanentEffect sparing(PermanentPredicate filter, PermanentSubject spared) {
        return new DestroyTargetPermanentEffect(
                Subjects.except(Subjects.controlledBy(Subjects.eachPlayer(), filter), spared));
    }

    /**
     * "Destroy all other creatures" after the tokens this resolution created (Martial Coup): pair it with an
     * unconditional {@code CreateTokenEffect} resolving before it.
     */
    public static DestroyTargetPermanentEffect sparingPermanentsCreatedThisResolution(PermanentPredicate filter) {
        return sparing(filter, Subjects.createdThisResolution());
    }

    /**
     * "Destroy all creatures with power greater than target creature's power": the wipe over {@code filter}, whose
     * amounts ({@code PermanentPowerAtLeastAmountPredicate} / {@code PermanentPowerAtMostAmountPredicate} over
     * {@code TargetPower}) read the target {@code target} declares.
     */
    public static DestroyTargetPermanentEffect readingTarget(PermanentPredicate filter, TargetPredicate target) {
        return new DestroyTargetPermanentEffect(false, null, -1, null, false, 1, false,
                Subjects.controlledBy(Subjects.eachPlayer(), filter), TargetSpec.harmful(target));
    }

    /** "Destroy each of them": every target of the entry, whichever group declared it, as one event. */
    public static DestroyTargetPermanentEffect eachTarget() {
        return new DestroyTargetPermanentEffect(Subjects.allTargets());
    }

    /** {@link #eachTarget()}; {@code cannotBeRegenerated} for "They can't be regenerated." */
    public static DestroyTargetPermanentEffect eachTarget(boolean cannotBeRegenerated) {
        return new DestroyTargetPermanentEffect(Subjects.allTargets(), cannotBeRegenerated);
    }

    /**
     * "Destroy any of them that are Walls": the targets that match {@code filter} as the effect applies; the others
     * stay (the filter is not a target restriction).
     */
    public static DestroyTargetPermanentEffect eachTargetMatching(PermanentPredicate filter) {
        return new DestroyTargetPermanentEffect(Subjects.allTargets(filter));
    }

    public static DestroyTargetPermanentEffect forTargetGroup(int targetGroup) {
        return new DestroyTargetPermanentEffect(false, null, targetGroup, null, false, 1, false);
    }

    public static DestroyTargetPermanentEffect activePlayerChoosesTarget(boolean cannotBeRegenerated,
                                                                           PermanentPredicate targetFilter) {
        return new DestroyTargetPermanentEffect(
                cannotBeRegenerated, null, -1, targetFilter, false, 1, true);
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject.declaresPlayerTarget()) {
            return TargetSpec.harmful(TargetPredicates.player());
        }
        if (!subject.declaresTarget()) {
            return referencedTarget != null ? referencedTarget : TargetSpec.NONE;
        }
        return targetFilter == null
                ? TargetSpec.harmful(TargetPredicates.permanent())
                : TargetSpec.harmful(TargetPredicates.permanent(), targetFilter);
    }

    /** Single-target removal only when it targets; "destroy this", "destroy enchanted creature" are not. */
    @Override
    public RemovalKind removalKind() {
        return subject.declaresTarget() ? RemovalKind.DESTROY : null;
    }

    /** "Destroy all [permanents]" over any players' battlefields, spared ones or not, sweeps the board. */
    @Override
    public boolean sweepsBoard() {
        return setFilter() != null;
    }

    /**
     * The filter of the set a wipe destroys ("all creatures", "the rest" of them), or {@code null} when the subject
     * is no {@code controlledBy} set.
     */
    public PermanentPredicate setFilter() {
        PermanentSubject set = subject instanceof PermanentSubject.Except except ? except.permanents() : subject;
        return set instanceof PermanentSubject.ControlledBy controlledBy ? controlledBy.filter() : null;
    }
}

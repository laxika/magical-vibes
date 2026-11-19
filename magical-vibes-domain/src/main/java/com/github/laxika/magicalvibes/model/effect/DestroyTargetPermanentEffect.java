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
 * <p>Only a subject with a target part targets ({@link #targetSpec()} is then harmful, narrowed by
 * {@code targetFilter}); any other subject declares no target, so protection, shroud and hexproof don't stop it and
 * a permanent that is gone at resolution is simply not destroyed.
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
 */
public record DestroyTargetPermanentEffect(
        boolean cannotBeRegenerated,
        CreateTokenEffect tokenForController,
        int targetGroup,
        PermanentPredicate targetFilter,
        boolean tokenOnlyForSourceController,
        int tokenCount,
        boolean targetChosenByActivePlayer,
        PermanentSubject subject
) implements RemovalEffect {

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
        if (!subject.declaresTarget()) {
            return TargetSpec.NONE;
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
}

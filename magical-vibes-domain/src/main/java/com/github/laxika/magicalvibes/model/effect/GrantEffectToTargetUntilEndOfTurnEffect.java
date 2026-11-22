package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;

/**
 * When resolved, grants the targeted creature, or the permanents {@code subject} names, an ability until end of turn.
 * The grant is a floating layer-6 effect on the permanent ({@code GameData.grantAbilityToPermanent}) that the
 * cleanup step ends.
 *
 * <p>Example: Verdant Rebirth grants "when this dies" +
 * {@link ReturnSourceCardFromGraveyardToOwnerHandEffect} until end of turn.
 *
 * @param grantedAbility the granted ability
 * @param delayedTrigger registers a spell-sourced delayed trigger watching the target instead
 *                       of granting that permanent an ability; target only
 * @param anyPermanent   whether the target is any permanent rather than a creature; target only
 * @param subject        who gains the ability: the target, {@link Subjects#source()}, or a set ("creatures you
 *                       control"), which is found once as the effect resolves (CR 611.2c)
 */
public record GrantEffectToTargetUntilEndOfTurnEffect(
        Ability grantedAbility,
        boolean delayedTrigger,
        boolean anyPermanent,
        PermanentSubject subject
) implements CardEffect {

    public GrantEffectToTargetUntilEndOfTurnEffect {
        if (!subject.equals(Subjects.target())) {
            if (!subject.isSet() && !(subject instanceof PermanentSubject.Source) || subject.declaresTarget()) {
                throw new IllegalArgumentException("Unsupported grant subject: " + subject);
            }
            if (delayedTrigger || anyPermanent) {
                throw new IllegalArgumentException("Only a target is watched or may be any permanent: " + subject);
            }
        }
    }

    /** The grant of {@code grantedAbility} to the target. */
    public GrantEffectToTargetUntilEndOfTurnEffect(Ability grantedAbility, boolean delayedTrigger,
                                                   boolean anyPermanent) {
        this(grantedAbility, delayedTrigger, anyPermanent, Subjects.target());
    }

    /** The grant of {@code grantedAbility} to the permanents {@code subject} names. */
    public GrantEffectToTargetUntilEndOfTurnEffect(Ability grantedAbility, PermanentSubject subject) {
        this(grantedAbility, false, false, subject);
    }

    /** The grant of {@code grantedAbility}. */
    public GrantEffectToTargetUntilEndOfTurnEffect(Ability grantedAbility, boolean delayedTrigger) {
        this(grantedAbility, delayedTrigger, false);
    }

    /** The grant of {@code grantedAbility}. */
    public GrantEffectToTargetUntilEndOfTurnEffect(Ability grantedAbility) {
        this(grantedAbility, false);
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.AttachedAndSharingCreatureType;
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget()
                ? TargetSpec.benign(anyPermanent ? TargetPredicates.permanent() : TargetPredicates.creature())
                : TargetSpec.NONE;
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;

/**
 * When resolved, grants the targeted creature a temporary triggered ability until end of turn.
 * The grant is a floating layer-6 effect on the permanent ({@code GameData.grantAbilityToPermanent}) that the
 * cleanup step ends.
 *
 * <p>Example: Verdant Rebirth grants "when this dies" +
 * {@link ReturnSourceCardFromGraveyardToOwnerHandEffect} until end of turn.
 *
 * @param grantedAbility the granted ability
 * @param delayedTrigger registers a spell-sourced delayed trigger watching the target instead
 *                       of granting that permanent an ability
 */
public record GrantEffectToTargetUntilEndOfTurnEffect(
        Ability grantedAbility,
        boolean delayedTrigger,
        boolean anyPermanent
) implements CardEffect {

    /** The grant of {@code grantedAbility}. */
    public GrantEffectToTargetUntilEndOfTurnEffect(Ability grantedAbility, boolean delayedTrigger) {
        this(grantedAbility, delayedTrigger, false);
    }

    /** The grant of {@code grantedAbility}. */
    public GrantEffectToTargetUntilEndOfTurnEffect(Ability grantedAbility) {
        this(grantedAbility, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(anyPermanent ? TargetPredicates.permanent() : TargetPredicates.creature());
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}

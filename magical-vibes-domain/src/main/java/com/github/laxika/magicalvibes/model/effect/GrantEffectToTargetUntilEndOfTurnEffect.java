package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;

/**
 * When resolved, grants the targeted creature a temporary triggered ability until end of turn.
 * The granted effect is stored on the permanent via
 * {@link com.github.laxika.magicalvibes.model.Permanent#addTemporaryTriggeredEffect(EffectSlot, CardEffect)}
 * and cleared at the cleanup step by {@link com.github.laxika.magicalvibes.model.Permanent#resetModifiers()}.
 *
 * <p>Example: Verdant Rebirth grants {@link EffectSlot#ON_DEATH} +
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

    /** The grant as the slot and effect that stand for it. */
    public GrantEffectToTargetUntilEndOfTurnEffect(EffectSlot slot,
                                                   CardEffect grantedEffect,
                                                   boolean delayedTrigger,
                                                   boolean anyPermanent) {
        this(LegacySlots.grant(slot, grantedEffect), delayedTrigger, anyPermanent);
    }

    public GrantEffectToTargetUntilEndOfTurnEffect(EffectSlot slot, CardEffect grantedEffect, boolean delayedTrigger) {
        this(slot, grantedEffect, delayedTrigger, false);
    }

    /** The grant of {@code grantedAbility}, such as a static ability, which has no slot. */
    public GrantEffectToTargetUntilEndOfTurnEffect(Ability grantedAbility, boolean delayedTrigger) {
        this(grantedAbility, delayedTrigger, false);
    }

    public GrantEffectToTargetUntilEndOfTurnEffect(EffectSlot slot, CardEffect grantedEffect) {
        this(slot, grantedEffect, false);
    }

    /** The grant of {@code grantedAbility}, such as a static ability, which has no slot. */
    public GrantEffectToTargetUntilEndOfTurnEffect(Ability grantedAbility) {
        this(grantedAbility, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(anyPermanent ? TargetPredicates.permanent() : TargetPredicates.creature());
    }

    /** The slot of {@link #grantedAbility}, or {@code null} when there is none. */
    public EffectSlot slot() {
        return grantedAbility == null ? null : LegacySlots.slotOf(grantedAbility);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : LegacySlots.effectsOf(grantedAbility).getFirst();
    }
}

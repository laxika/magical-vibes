package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;

/**
 * When resolved, grants the targeted permanent a triggered ability for the given slot.
 * Duration {@link EffectDuration#PERMANENT} stores it on
 * {@link com.github.laxika.magicalvibes.model.Permanent#addPersistentTriggeredEffect} (survives
 * end-of-turn cleanup). Duration {@link EffectDuration#UNTIL_END_OF_TURN} uses
 * {@link com.github.laxika.magicalvibes.model.Permanent#addTemporaryTriggeredEffect} (cleared by
 * {@code resetModifiers()}).
 *
 * <p>Example: Balduvian Shaman grants {@link EffectSlot#UPKEEP_TRIGGERED} +
 * {@link CumulativeUpkeepEffect} indefinitely.
 *
 * @param grantedAbility the granted ability
 * @param duration       how long the grant lasts
 * @param scope          which permanent receives the ability: {@link GrantScope#TARGET} (the chosen
 *                       target), {@link GrantScope#SELF} (the source permanent), or
 *                       {@link GrantScope#ENCHANTED_PERMANENT} (the permanent the source
 *                       Aura is attached to, re-derived at resolution and without targeting — the
 *                       Genju cycle's granted "whenever this creature deals damage" ability)
 */
public record GrantEffectToTargetEffect(
        Ability grantedAbility,
        EffectDuration duration,
        boolean skipIfAlreadyPresent,
        GrantScope scope
) implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public GrantEffectToTargetEffect(EffectSlot slot,
                                     CardEffect grantedEffect,
                                     EffectDuration duration,
                                     boolean skipIfAlreadyPresent,
                                     GrantScope scope) {
        this(LegacySlots.grant(slot, grantedEffect), duration, skipIfAlreadyPresent, scope);
    }

    /** Indefinite grant (Balduvian Shaman). */
    public GrantEffectToTargetEffect(EffectSlot slot, CardEffect grantedEffect) {
        this(slot, grantedEffect, EffectDuration.PERMANENT, true);
    }

    /** Grant with an explicit duration, skipping equal existing grants by default. */
    public GrantEffectToTargetEffect(EffectSlot slot, CardEffect grantedEffect, EffectDuration duration) {
        this(slot, grantedEffect, duration, true);
    }

    public GrantEffectToTargetEffect(EffectSlot slot, CardEffect grantedEffect, EffectDuration duration,
                                     boolean skipIfAlreadyPresent) {
        this(slot, grantedEffect, duration, skipIfAlreadyPresent, GrantScope.TARGET);
    }

    /** Indefinite grant to the source permanent (Kellan, Planar Trailblazer). */
    public static GrantEffectToTargetEffect toSourcePermanent(EffectSlot slot, CardEffect grantedEffect) {
        return new GrantEffectToTargetEffect(slot, grantedEffect, EffectDuration.PERMANENT, true,
                GrantScope.SELF);
    }

    /** Grant to the permanent the source Aura enchants, for the given duration (Genju cycle). */
    public static GrantEffectToTargetEffect toEnchantedPermanent(EffectSlot slot, CardEffect grantedEffect,
                                                                 EffectDuration duration) {
        return new GrantEffectToTargetEffect(slot, grantedEffect, duration, false, GrantScope.ENCHANTED_PERMANENT);
    }

    /** Grants a triggered ability to the resolving source permanent without targeting it. */
    public static GrantEffectToTargetEffect toSource(EffectSlot slot, CardEffect grantedEffect,
                                                     EffectDuration duration) {
        return new GrantEffectToTargetEffect(slot, grantedEffect, duration, true, GrantScope.SELF);
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.TARGET ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
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

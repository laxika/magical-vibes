package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;

/**
 * When resolved, grants the targeted permanent an ability.
 * Duration {@link Duration#PERMANENT} stores it on
 * {@link com.github.laxika.magicalvibes.model.Permanent#addPersistentAbility} (survives
 * end-of-turn cleanup). Duration {@link Duration#UNTIL_END_OF_TURN} uses
 * {@link com.github.laxika.magicalvibes.model.Permanent#addTemporaryAbility} (cleared by
 * {@code resetModifiers()}).
 *
 * <p>Example: Balduvian Shaman grants {@code Triggers.upkeep(ActivePlayer.YOU)} +
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
        Duration duration,
        boolean skipIfAlreadyPresent,
        GrantScope scope
) implements CardEffect {

    /** The grant of {@code grantedAbility}. */
    public GrantEffectToTargetEffect(Ability grantedAbility) {
        this(grantedAbility, Duration.PERMANENT, true);
    }

    /** The grant of {@code grantedAbility}. */
    public GrantEffectToTargetEffect(Ability grantedAbility, Duration duration) {
        this(grantedAbility, duration, true);
    }

    /** The grant of {@code grantedAbility}. */
    public GrantEffectToTargetEffect(Ability grantedAbility, Duration duration,
                                     boolean skipIfAlreadyPresent) {
        this(grantedAbility, duration, skipIfAlreadyPresent, GrantScope.TARGET);
    }

    /** Indefinite grant of {@code grantedAbility} to the source permanent. */
    public static GrantEffectToTargetEffect toSourcePermanent(Ability grantedAbility) {
        return new GrantEffectToTargetEffect(grantedAbility, Duration.PERMANENT, true, GrantScope.SELF);
    }

    /** Grant to the permanent the source Aura enchants, for the given duration (Genju cycle). */
    public static GrantEffectToTargetEffect toEnchantedPermanent(Ability grantedAbility, Duration duration) {
        return new GrantEffectToTargetEffect(grantedAbility, duration, false, GrantScope.ENCHANTED_PERMANENT);
    }

    /** Grants {@code grantedAbility} to the resolving source permanent without targeting it. */
    public static GrantEffectToTargetEffect toSource(Ability grantedAbility, Duration duration) {
        return new GrantEffectToTargetEffect(grantedAbility, duration, true, GrantScope.SELF);
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.TARGET ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}

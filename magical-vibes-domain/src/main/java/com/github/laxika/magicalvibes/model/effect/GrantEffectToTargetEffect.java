package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;

/**
 * When resolved, grants the targeted permanent an ability: a floating layer-6 effect on it
 * ({@code GameData.grantAbilityToPermanent}) for the stated duration, or with none stated
 * ({@link Duration#PERMANENT}).
 *
 * <p>Example: Balduvian Shaman grants {@code Triggers.upkeep(ActivePlayer.YOU)} +
 * {@link CumulativeUpkeepEffect} indefinitely.
 *
 * @param grantedAbility the granted ability
 * @param duration       how long the grant lasts
 * @param subject          which permanent receives the ability: {@link Subjects#target()} (the chosen
 *                       target), {@link Subjects#source()} (the source permanent), or
 *                       {@link Subjects#attached()} (the permanent the source
 *                       Aura is attached to, re-derived at resolution and without targeting — the
 *                       Genju cycle's granted "whenever this creature deals damage" ability), or any
 *                       other subject that doesn't target, found once as the effect resolves (CR 611.2c)
 */
public record GrantEffectToTargetEffect(
        Ability grantedAbility,
        Duration duration,
        boolean skipIfAlreadyPresent,
        PermanentSubject subject
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
        this(grantedAbility, duration, skipIfAlreadyPresent, Subjects.target());
    }

    /** Indefinite grant of {@code grantedAbility} to the source permanent. */
    public static GrantEffectToTargetEffect toSourcePermanent(Ability grantedAbility) {
        return new GrantEffectToTargetEffect(grantedAbility, Duration.PERMANENT, true, Subjects.source());
    }

    /** Grant to the permanent the source Aura enchants, for the given duration (Genju cycle). */
    public static GrantEffectToTargetEffect toEnchantedPermanent(Ability grantedAbility, Duration duration) {
        return new GrantEffectToTargetEffect(grantedAbility, duration, false, Subjects.attached());
    }

    /** Grants {@code grantedAbility} to the resolving source permanent without targeting it. */
    public static GrantEffectToTargetEffect toSource(Ability grantedAbility, Duration duration) {
        return new GrantEffectToTargetEffect(grantedAbility, duration, true, Subjects.source());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.NONE;
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}

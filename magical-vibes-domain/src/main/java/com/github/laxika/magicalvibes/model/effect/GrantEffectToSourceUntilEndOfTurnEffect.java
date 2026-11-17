package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;

/**
 * When resolved, grants the source permanent itself a temporary triggered ability until end of
 * turn (e.g. Cruel Deceiver's "this creature gains '…' until end of turn"). Non-targeting — the
 * recipient is the resolving stack entry's source permanent, so nothing is chosen or validated.
 *
 * <p>The granted ability is stored via
 * {@link com.github.laxika.magicalvibes.model.Permanent#addTemporaryAbility}
 * and cleared at cleanup by {@link com.github.laxika.magicalvibes.model.Permanent#resetModifiers()}.
 *
 * @param grantedAbility the granted ability
 */
public record GrantEffectToSourceUntilEndOfTurnEffect(
        Ability grantedAbility
) implements CardEffect {

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}

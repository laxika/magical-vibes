package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * When resolved, grants every creature the controller currently controls a temporary triggered
 * ability until end of turn. Creatures that enter (or change control) later this turn do not
 * receive it — snapshot at resolution (e.g. Driven / Despair).
 *
 * <p>Each grant is a floating layer-6 effect on its creature ({@code GameData.grantAbilityToPermanent}) that the
 * cleanup step ends.
 *
 * @param grantedAbility the granted ability
 * @param filter        optional filter limiting the snapshot of creatures receiving the trigger
 */
public record GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
        Ability grantedAbility,
        PermanentPredicate filter
) implements CardEffect {

    /** The grant of {@code grantedAbility}. */
    public GrantEffectToOwnCreaturesUntilEndOfTurnEffect(Ability grantedAbility) {
        this(grantedAbility, null);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}

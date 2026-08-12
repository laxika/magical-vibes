package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that prevents all damage dealt to this permanent by spells that target it.
 */
public record PreventDamageToSelfFromTargetingSpellsEffect(PermanentPredicate condition)
        implements TargetedSpellDamagePreventionEffect {

    public PreventDamageToSelfFromTargetingSpellsEffect() {
        this(null);
    }
}

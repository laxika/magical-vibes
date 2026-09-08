package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability for a static effect that prevents damage to its permanent from spells targeting it.
 */
public interface TargetedSpellDamagePreventionEffect extends CardEffect {

    /** Returns the condition that must currently match the protected permanent, or {@code null}. */
    PermanentPredicate condition();
}

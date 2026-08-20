package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Capability for static effects that prevent damage dealt by their source to matching creatures. */
public interface DamagePreventionBySelfEffect extends CardEffect {

    /** Returns the additional filter for creatures receiving the prevented damage. */
    PermanentPredicate targetFilter();
}

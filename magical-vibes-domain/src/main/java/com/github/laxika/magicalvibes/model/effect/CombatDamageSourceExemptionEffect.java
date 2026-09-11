package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Capability for continuous effects that exempt matching creature sources from combat-damage prevention. */
public interface CombatDamageSourceExemptionEffect extends CardEffect {

    PermanentPredicate exemptSourcePredicate();
}

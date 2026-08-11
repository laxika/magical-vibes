package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability for an attack-only restriction that requires another declared attacker matching a
 * permanent predicate. The combat service evaluates the predicate against the other attackers in
 * the same declaration.
 */
public interface MatchingAttackerRestrictionEffect extends CardEffect {

    PermanentPredicate matchingAttackerPredicate();

    String restrictionDescription();
}

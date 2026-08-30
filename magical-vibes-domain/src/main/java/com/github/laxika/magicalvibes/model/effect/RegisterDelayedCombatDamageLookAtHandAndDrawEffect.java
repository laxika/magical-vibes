package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Registers a rest-of-turn trigger for one or more matching creatures dealing combat damage to a player. */
public record RegisterDelayedCombatDamageLookAtHandAndDrawEffect(PermanentPredicate sourcePredicate)
        implements CardEffect {
}

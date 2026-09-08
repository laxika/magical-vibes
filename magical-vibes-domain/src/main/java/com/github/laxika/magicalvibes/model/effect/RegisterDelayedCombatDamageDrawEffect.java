package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Registers a rest-of-turn trigger that draws a card whenever a creature the controller controls
 * deals combat damage to a player or planeswalker. An optional source predicate and planeswalker
 * flag narrow the qualifying damage events.
 */
public record RegisterDelayedCombatDamageDrawEffect(
        PermanentPredicate sourcePredicate,
        boolean includesPlaneswalkers
) implements CardEffect {

    public RegisterDelayedCombatDamageDrawEffect() {
        this(null, true);
    }

    public RegisterDelayedCombatDamageDrawEffect(PermanentPredicate sourcePredicate) {
        this(sourcePredicate, true);
    }
}

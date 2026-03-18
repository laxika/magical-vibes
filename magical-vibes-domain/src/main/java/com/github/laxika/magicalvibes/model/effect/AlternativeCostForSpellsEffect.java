package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect from a permanent on the battlefield that allows the controller
 * to pay an alternative mana cost for spells matching the given predicate.
 * This is an alternative cost per CR 118.9.
 * (e.g. Rooftop Storm: "You may pay {0} rather than pay the mana cost for Zombie creature spells you cast.")
 */
public record AlternativeCostForSpellsEffect(int cost, CardPredicate filter) implements CardEffect {
}

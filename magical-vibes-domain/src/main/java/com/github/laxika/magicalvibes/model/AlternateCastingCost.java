package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Describes an alternate casting cost that can be paid instead of the card's normal mana cost.
 * For example, Demon of Death's Gate: "You may pay 6 life and sacrifice three black creatures
 * rather than pay this spell's mana cost."
 *
 * @param lifeCost        amount of life to pay
 * @param sacrificeCount  number of permanents to sacrifice
 * @param sacrificeFilter predicate that each sacrificed permanent must match
 */
public record AlternateCastingCost(int lifeCost, int sacrificeCount, PermanentPredicate sacrificeFilter) {
}

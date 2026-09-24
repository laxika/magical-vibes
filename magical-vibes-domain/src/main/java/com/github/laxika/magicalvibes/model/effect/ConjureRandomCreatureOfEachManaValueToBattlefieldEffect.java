package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * For each mana value from one through the evaluated amount, conjures a random creature card with
 * that exact mana value onto the battlefield.
 */
public record ConjureRandomCreatureOfEachManaValueToBattlefieldEffect(DynamicAmount maxManaValue)
        implements CardEffect {
}

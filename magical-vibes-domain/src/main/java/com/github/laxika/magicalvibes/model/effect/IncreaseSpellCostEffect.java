package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Increases the casting cost of matching spells for all players by the given amount.
 * E.g. Thalia, Guardian of Thraben: predicate = not-creature, amount = 1.
 */
public record IncreaseSpellCostEffect(CardPredicate predicate, int amount) implements CardEffect {
}

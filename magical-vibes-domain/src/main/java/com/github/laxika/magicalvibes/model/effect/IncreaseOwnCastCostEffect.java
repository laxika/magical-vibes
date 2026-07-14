package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Increases the casting cost of matching spells cast by the source permanent's controller by the
 * given amount. Unlike {@link IncreaseSpellCostEffect} (symmetric, all players), this taxes only
 * the controller's own spells. E.g. Derelor: predicate = black, amount = 1 ("Black spells you
 * cast cost {B} more to cast").
 */
public record IncreaseOwnCastCostEffect(CardPredicate predicate, int amount) implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Creates a temporary opponent-scoped cost increase lasting until the controller's next turn. */
public record IncreaseSpellCostUntilNextTurnEffect(CardPredicate predicate, int amount)
        implements CardEffect {
}

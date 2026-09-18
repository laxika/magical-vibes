package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Creates a temporary cost increase for spells cast by the player in the stack entry's targetId. */
public record IncreaseSpellCostForTargetPlayerUntilEndOfTurnEffect(CardPredicate predicate, int amount)
        implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reduces the colored mana components of matching spells cast by the selected players.
 *
 * <p>Unlike a generic cast-cost reduction, colored mana that does not match the spell's colored
 * requirements is not converted into a reduction of its generic mana component.
 */
public record ReduceColoredCastCostForMatchingSpellsEffect(
        CardPredicate predicate,
        ManaCost reduction,
        CostModificationScope scope
) implements CardEffect {
}

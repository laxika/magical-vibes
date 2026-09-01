package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reduces the colored mana components of matching spells cast by the selected players.
 *
 * <p>By default, colored mana that does not match the spell's colored requirements is not converted
 * into a reduction of its generic mana component. Effects that explicitly allow that conversion
 * set {@code canReduceGeneric} to {@code true}.
 */
public record ReduceColoredCastCostForMatchingSpellsEffect(
        CardPredicate predicate,
        ManaCost reduction,
        CostModificationScope scope,
        boolean canReduceGeneric
) implements CardEffect {

    public ReduceColoredCastCostForMatchingSpellsEffect(CardPredicate predicate, ManaCost reduction,
                                                        CostModificationScope scope) {
        this(predicate, reduction, scope, false);
    }
}

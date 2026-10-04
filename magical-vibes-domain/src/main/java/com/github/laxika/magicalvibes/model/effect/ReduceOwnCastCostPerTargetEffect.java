package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reduces this spell's generic casting cost by {@code amount} for every chosen target permanent
 * matching {@code predicate}.
 */
public record ReduceOwnCastCostPerTargetEffect(PermanentPredicate predicate, int amount,
                                              CardPredicate spellPredicate)
        implements PerTargetCastCostReductionEffect {

    public ReduceOwnCastCostPerTargetEffect(PermanentPredicate predicate, int amount) {
        this(predicate, amount, null);
    }
}

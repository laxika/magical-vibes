package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Describes a cost reduction counted once for every distinct target permanent matching the
 * predicate. An optional spell predicate limits the spells to which the reduction applies.
 */
public interface PerTargetCastCostReductionEffect extends CardEffect {

    PermanentPredicate predicate();

    int amount();

    default CardPredicate spellPredicate() {
        return null;
    }
}

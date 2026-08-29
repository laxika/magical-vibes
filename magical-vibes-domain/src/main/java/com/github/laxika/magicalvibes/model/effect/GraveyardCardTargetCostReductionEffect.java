package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Common capability for a spell-self cost reduction gated by a graveyard-card target. */
public interface GraveyardCardTargetCostReductionEffect extends CardEffect {

    CardPredicate predicate();

    int amount();
}

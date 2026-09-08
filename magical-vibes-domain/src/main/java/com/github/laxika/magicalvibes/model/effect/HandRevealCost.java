package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Common contract for activated-ability costs that reveal cards from the controller's hand. */
public interface HandRevealCost extends CostEffect {

    CardPredicate filter();
}

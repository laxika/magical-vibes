package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Capability for a static effect that grants blitz to matching creature spells. */
public interface BlitzGrantingEffect extends CardEffect {

    CardPredicate blitzGrantFilter();

    DynamicAmount blitzCostReduction();
}

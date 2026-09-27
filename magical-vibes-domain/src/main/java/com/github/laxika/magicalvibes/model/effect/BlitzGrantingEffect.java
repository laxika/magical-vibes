package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Capability for a static effect that grants blitz to matching creature spells. */
public interface BlitzGrantingEffect extends CardEffect {

    CardPredicate blitzGrantFilter();

    /** Fixed blitz cost, or null when the card's mana cost is used. */
    String blitzCost();

    /** Reduction to the granted blitz cost, or null when no reduction applies. */
    DynamicAmount blitzCostReduction();
}

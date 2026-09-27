package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Capability for a static effect that grants miracle to matching cards in the controller's hand.
 * The draw service reads the filter when the first card is drawn and snapshots the cost onto the
 * miracle trigger.
 */
public interface MiracleGrantingEffect extends CardEffect {

    CardPredicate miracleGrantFilter();

    default String miracleCost() {
        return null;
    }

    /** Returns the cost to snapshot for this particular card. */
    default String miracleCostFor(Card card) {
        return miracleCost();
    }
}

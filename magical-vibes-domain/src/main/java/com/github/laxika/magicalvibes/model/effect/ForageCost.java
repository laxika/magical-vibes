package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Forage": exile three cards from your graveyard or sacrifice a Food (CR 701.61a), the payer choosing which. A cast or
 * activation cost ("{2}, Forage:", "Kicker—Forage", Osteomancer Adept's graveyard permission); the Food comes through
 * the payment's permanent choice, the three cards through its graveyard choice, and paying it raises the foraged event.
 * "Forage or pay {B}" (Feed the Cycle) is an {@link AnyOfCost} with a {@link PayManaCost} alternative.
 */
public record ForageCost() implements CostEffect {

    private static final PermanentPredicate FOOD_FILTER = new PermanentHasSubtypePredicate(CardSubtype.FOOD);

    /** The number of graveyard cards the exile option of foraging exiles. */
    public static final int GRAVEYARD_CARD_COUNT = 3;

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return FOOD_FILTER;
    }

    @Override
    public int consumedGraveyardCardCount() {
        return GRAVEYARD_CARD_COUNT;
    }
}

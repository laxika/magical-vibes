package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additional cast cost that pays by foraging or by paying the listed mana cost.
 * Foraging means exiling exactly three cards from the payer's graveyard or sacrificing a Food.
 * A {@code null} mana cost means that only foraging is allowed.
 */
public record ForageOrPayManaCost(String manaCost) implements CostEffect {

    private static final PermanentPredicate FOOD_FILTER = new PermanentHasSubtypePredicate(CardSubtype.FOOD);

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return FOOD_FILTER;
    }

    @Override
    public int consumedGraveyardCardCount() {
        return 3;
    }

    public static ForageOrPayManaCost forageOnly() {
        return new ForageOrPayManaCost(null);
    }
}

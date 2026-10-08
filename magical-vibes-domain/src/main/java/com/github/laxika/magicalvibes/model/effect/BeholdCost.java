package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Additional cast cost to behold a number of distinct matching permanents and/or cards in hand.
 * Unlike {@link BeholdAndExileCost}, paying this cost does not move the chosen objects.
 */
public record BeholdCost(CardSubtype subtype, int count, boolean optional, boolean chosenCreatureType, String alternativeManaCost) implements CostEffect {

    public BeholdCost(CardSubtype subtype, int count, boolean optional, boolean chosenCreatureType) {
        this(subtype, count, optional, chosenCreatureType, null);
    }

    public BeholdCost(CardSubtype subtype) {
        this(subtype, 1, false, false);
    }

    public BeholdCost(CardSubtype subtype, int count) {
        this(subtype, count, false, false);
    }

    public static BeholdCost optional(CardSubtype subtype) {
        return new BeholdCost(subtype, 1, true, false);
    }

    public static BeholdCost orPayMana(CardSubtype subtype, String manaCost) {
        return new BeholdCost(subtype, 1, true, false, manaCost);
    }

    public static BeholdCost optionalChosenCreatureType(int count) {
        return new BeholdCost(null, count, true, true);
    }
}

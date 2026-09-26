package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Land-tap trigger: whenever a land taps for mana, the tapping player adds one mana of any type
 * that land produced. Used by Vorinclex, Voice of Hunger and similar "mana doubling" effects.
 *
 * <p>When {@code controllerOnly} is {@code true} the trigger fires only for lands the source's
 * controller taps (Vorinclex). When {@code false} it is symmetric — every player's land taps add
 * the extra mana to that player's pool (Mana Flare).</p>
 *
 * <p>{@code landFilter} optionally narrows which tapped lands trigger the effect.
 *
 * <p>Per MTG ruling: if the land produces multiple types, this adds one mana of only one
 * of those types (player's choice). Unlike {@link AddExtraManaOfChosenColorOnLandTapEffect},
 * this effect does not depend on a chosen color.</p>
 *
 * @param matchesImprintedCardName when {@code true}, only lands whose names match the card
 *                                imprinted on the source trigger
 * @param monarchOnly when {@code true}, the source controller must be the monarch when the land
 *                    is tapped
 */
public record AddOneOfEachManaTypeProducedByLandEffect(
        boolean controllerOnly,
        boolean matchesImprintedCardName,
        PermanentPredicate landFilter,
        boolean monarchOnly
) implements CardEffect {

    public AddOneOfEachManaTypeProducedByLandEffect(boolean controllerOnly) {
        this(controllerOnly, false, null, false);
    }

    public AddOneOfEachManaTypeProducedByLandEffect(boolean controllerOnly,
                                                     boolean matchesImprintedCardName) {
        this(controllerOnly, matchesImprintedCardName, null, false);
    }

    public AddOneOfEachManaTypeProducedByLandEffect(boolean controllerOnly,
                                                     PermanentPredicate landFilter) {
        this(controllerOnly, false, landFilter, false);
    }

    public AddOneOfEachManaTypeProducedByLandEffect(boolean controllerOnly,
                                                     PermanentPredicate landFilter,
                                                     boolean monarchOnly) {
        this(controllerOnly, false, landFilter, monarchOnly);
    }
}

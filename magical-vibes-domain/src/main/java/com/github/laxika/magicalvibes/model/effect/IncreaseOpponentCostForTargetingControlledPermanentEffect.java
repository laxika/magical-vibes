package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static ability that increases the cost of opponent spells and activated abilities
 * that target a permanent matching the predicate and controlled by this effect's controller.
 * E.g. Kopala, Warden of Waves: {@code PermanentHasSubtypePredicate(MERFOLK)}, amount = 2.
 * The optional ability flag is false for effects that tax only spells, such as Boreal Elemental.
 */
public record IncreaseOpponentCostForTargetingControlledPermanentEffect(
        PermanentPredicate predicate, int amount, boolean taxesActivatedAbilities) implements CardEffect {

    public IncreaseOpponentCostForTargetingControlledPermanentEffect(PermanentPredicate predicate, int amount) {
        this(predicate, amount, true);
    }
}

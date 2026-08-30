package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/**
 * Static ability that increases the cost of opponent spells and activated abilities
 * that target a permanent matching the predicate and controlled by this effect's controller.
 * E.g. Kopala, Warden of Waves: {@code PermanentHasSubtypePredicate(MERFOLK)}, amount = 2.
 * The optional ability flag is false for effects that tax only spells, such as Boreal Elemental.
 * When {@code taxesController} is true, spells and abilities targeting the effect's controller
 * are taxed as well.
 */
public record IncreaseOpponentCostForTargetingControlledPermanentEffect(
        PermanentPredicate predicate, int amount, boolean taxesActivatedAbilities,
        boolean taxesController) implements CardEffect {

    public IncreaseOpponentCostForTargetingControlledPermanentEffect(PermanentPredicate predicate, int amount) {
        this(predicate, amount, true, false);
    }

    public IncreaseOpponentCostForTargetingControlledPermanentEffect(
            PermanentPredicate predicate, int amount, boolean taxesActivatedAbilities) {
        this(predicate, amount, taxesActivatedAbilities, false);
    }

    /** Spell-only tax for spells targeting this effect's controller or any permanent they control. */
    public static IncreaseOpponentCostForTargetingControlledPermanentEffect
    forControllerAndControlledPermanents(int amount) {
        return new IncreaseOpponentCostForTargetingControlledPermanentEffect(
                new PermanentTruePredicate(), amount, false, true);
    }
}

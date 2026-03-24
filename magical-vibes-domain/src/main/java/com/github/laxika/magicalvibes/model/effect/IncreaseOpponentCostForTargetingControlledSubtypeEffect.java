package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Static ability that increases the cost of opponent spells and activated abilities
 * that target a permanent with the specified subtype controlled by this effect's controller.
 * E.g. Kopala, Warden of Waves: subtype = MERFOLK, amount = 2.
 */
public record IncreaseOpponentCostForTargetingControlledSubtypeEffect(CardSubtype subtype, int amount) implements CardEffect {
}

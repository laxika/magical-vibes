package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Reduces this spell's casting cost by the given amount if the controller controls at least
 * one permanent with the specified subtype. E.g. Academy Journeymage costs {1} less if you
 * control a Wizard.
 */
public record ReduceOwnCastCostIfControlsSubtypeEffect(CardSubtype subtype, int amount) implements CardEffect {
}

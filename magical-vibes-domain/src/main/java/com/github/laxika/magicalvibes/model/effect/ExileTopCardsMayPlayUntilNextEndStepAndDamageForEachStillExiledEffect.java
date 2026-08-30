package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Exiles the top cards of the controller's library, grants play permission until that player's
 * next end step, and creates a delayed trigger that deals damage for the cards still exiled.
 */
public record ExileTopCardsMayPlayUntilNextEndStepAndDamageForEachStillExiledEffect(
        DynamicAmount count, int damagePerCard) implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Marks a spell effect whose X value is calculated from game state as the spell is cast.
 * The cast path evaluates this amount before validating or selecting targets.
 */
public interface CastTimeXValueEffect extends CardEffect {

    DynamicAmount castTimeXValue();
}

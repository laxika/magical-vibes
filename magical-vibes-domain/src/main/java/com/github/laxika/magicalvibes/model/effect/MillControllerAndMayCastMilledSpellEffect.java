package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Mills the controller's library, then offers one eligible milled spell for free. */
public record MillControllerAndMayCastMilledSpellEffect(DynamicAmount count) implements CardEffect {
}

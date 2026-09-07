package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Draws one card when the source permanent's controller has at least as much life as the value
 * stored on that permanent, then replaces the stored note with the controller's current life.
 */
public record DrawCardIfControllerLifeTotalAtLeastNotedEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Draws one card for the player the entry records as "that player": the monarch whose end step it is (the monarch's
 * own draw, and Archivist of Gondor's "at the beginning of the monarch's end step", an event condition).
 */
public record DrawCardForMonarchEndStepEffect()
        implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Makes the controller and defending player each draw and discard a card. If the controller's
 * discarded card has the greatest mana value, or is tied for greatest, the source gets two
 * +1/+1 counters.
 */
public record CaitCageBrawlerEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}

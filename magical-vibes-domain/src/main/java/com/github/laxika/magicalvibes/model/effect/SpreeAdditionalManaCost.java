package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Carries one additional mana cost for each mode of a Spree spell, in modal option order.
 */
public record SpreeAdditionalManaCost(List<String> modeManaCosts) implements CostEffect {

    public SpreeAdditionalManaCost {
        modeManaCosts = List.copyOf(modeManaCosts);
    }
}

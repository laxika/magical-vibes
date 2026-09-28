package com.github.laxika.magicalvibes.model.effect;

/**
 * Serum Powder's optional hand replacement during the mulligan decision.
 */
public record SerumPowderEffect() implements CardEffect {

    @Override
    public String mulliganActionDescription() {
        return "Exile your hand and draw that many cards?";
    }
}

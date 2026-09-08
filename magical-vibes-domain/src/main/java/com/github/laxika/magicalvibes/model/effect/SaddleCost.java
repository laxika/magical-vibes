package com.github.laxika.magicalvibes.model.effect;

/** Cost for the Saddle keyword ability on Mounts. */
public record SaddleCost(int requiredPower) implements PowerBasedTapCost {

    @Override
    public String paymentNoun() {
        return "saddle";
    }
}

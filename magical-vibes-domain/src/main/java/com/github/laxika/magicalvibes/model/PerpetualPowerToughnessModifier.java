package com.github.laxika.magicalvibes.model;

/** A power/toughness modification that remains attached to a card as it changes zones. */
public record PerpetualPowerToughnessModifier(int power, int toughness) {

    public PerpetualPowerToughnessModifier add(PerpetualPowerToughnessModifier other) {
        return new PerpetualPowerToughnessModifier(
                power + other.power(), toughness + other.toughness());
    }
}

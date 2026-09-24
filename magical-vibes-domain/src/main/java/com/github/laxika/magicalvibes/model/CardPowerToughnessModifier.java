package com.github.laxika.magicalvibes.model;

/** A cumulative power/toughness modifier attached to a physical card identity. */
public record CardPowerToughnessModifier(int power, int toughness) {

    public CardPowerToughnessModifier add(int power, int toughness) {
        return new CardPowerToughnessModifier(this.power + power, this.toughness + toughness);
    }
}

package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: this creature gets +X/+Y where X is the imprinted creature card's power
 * and Y is its toughness. Used by Phyrexian Ingester.
 */
public record BoostSelfByImprintedCreaturePTEffect() implements CardEffect {
}

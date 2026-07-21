package com.github.laxika.magicalvibes.model.effect;

/**
 * Static anthem: each <em>other</em> multicolored creature its controller controls gets
 * +{@code powerPerColor}/+{@code toughnessPerColor} for each of that creature's colors
 * (Knight of New Alara). Monocolored and colorless creatures are unaffected, and the source
 * permanent itself never receives the boost.
 */
public record BoostOtherMulticoloredCreaturesByColorCountEffect(
        int powerPerColor,
        int toughnessPerColor
) implements CardEffect {
}

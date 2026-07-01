package com.github.laxika.magicalvibes.model.effect;

/**
 * "This land enters tapped unless you control [minOtherLands] or more other lands."
 * Used by the Innistrad "slow land" cycle (minOtherLands = 2).
 */
public record EntersTappedUnlessManyLandsEffect(int minOtherLands) implements ReplacementEffect {
}

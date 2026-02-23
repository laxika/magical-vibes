package com.github.laxika.magicalvibes.model.effect;

/**
 * "This land enters tapped unless you control [maxOtherLands] or fewer other lands."
 * Used by the Scars of Mirrodin "fast land" cycle (maxOtherLands = 2).
 */
public record EntersTappedUnlessFewLandsEffect(int maxOtherLands) implements CardEffect {
}

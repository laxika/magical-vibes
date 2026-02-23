package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/**
 * Metalcraft — this creature has the given keyword (and optionally +power/+toughness)
 * as long as you control three or more artifacts. Self-only static effect.
 */
public record MetalcraftKeywordEffect(Keyword keyword, int powerBoost, int toughnessBoost) implements CardEffect {
    public MetalcraftKeywordEffect(Keyword keyword) {
        this(keyword, 0, 0);
    }
}

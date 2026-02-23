package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/**
 * Metalcraft — this creature has the given keyword as long as you control three or more artifacts.
 * Self-only static effect.
 */
public record MetalcraftKeywordEffect(Keyword keyword) implements CardEffect {
}

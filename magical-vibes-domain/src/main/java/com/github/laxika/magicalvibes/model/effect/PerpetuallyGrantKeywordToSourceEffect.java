package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/** Perpetually adds a keyword to the source card, preserving it through zone changes. */
public record PerpetuallyGrantKeywordToSourceEffect(Keyword keyword) implements CardEffect {
}

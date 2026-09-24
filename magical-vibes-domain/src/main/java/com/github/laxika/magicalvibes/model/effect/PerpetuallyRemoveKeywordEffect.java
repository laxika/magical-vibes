package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/** Permanently removes a keyword from the source card identity in every zone. */
public record PerpetuallyRemoveKeywordEffect(Keyword keyword) implements CardEffect {
}

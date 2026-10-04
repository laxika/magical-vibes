package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;

/** Matches a keyword, optionally requiring a particular restricted hexproof variant. */
public record CardKeywordPredicate(Keyword keyword, TargetingRestrictionEffect restriction) implements CardPredicate {

    public CardKeywordPredicate(Keyword keyword) {
        this(keyword, null);
    }
}

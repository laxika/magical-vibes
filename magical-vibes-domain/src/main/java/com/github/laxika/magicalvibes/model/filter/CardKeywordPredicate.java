package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.Keyword;

public record CardKeywordPredicate(Keyword keyword) implements CardPredicate {
}

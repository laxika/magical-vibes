package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.Keyword;

public record PermanentHasKeywordPredicate(Keyword keyword) implements PermanentPredicate {
}

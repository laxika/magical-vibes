package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.Keyword;

public record PermanentHasKeywordPredicate(Keyword keyword, boolean printedOnly) implements PermanentPredicate {

    public PermanentHasKeywordPredicate(Keyword keyword) {
        this(keyword, false);
    }
}

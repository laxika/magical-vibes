package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Sacrifice one matching permanent, then grant keyword(s) to the source permanent until end of
 * turn. The grant is applied synchronously with the sacrifice for "if you do" wording.
 */
public record SacrificePermanentAndGrantKeywordSelfEffect(
        PermanentPredicate sacrificeFilter,
        Set<Keyword> keywords,
        String permanentDescription
) implements KeywordGrantingEffect {

    public SacrificePermanentAndGrantKeywordSelfEffect(PermanentPredicate sacrificeFilter,
                                                        Keyword keyword,
                                                        String permanentDescription) {
        this(sacrificeFilter, Set.of(keyword), permanentDescription);
    }

    @Override
    public GrantScope scope() {
        return GrantScope.SELF;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }
}

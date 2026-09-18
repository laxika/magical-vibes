package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/** Grants keywords to your creatures whose power is at most the power of the effect's target. */
public record GrantKeywordsToOwnCreaturesWithPowerAtMostTargetEffect(Set<Keyword> keywords)
        implements KeywordGrantingEffect {

    public GrantKeywordsToOwnCreaturesWithPowerAtMostTargetEffect(Keyword keyword) {
        this(Set.of(keyword));
    }

    public GrantKeywordsToOwnCreaturesWithPowerAtMostTargetEffect {
        keywords = Set.copyOf(keywords);
        if (keywords.isEmpty()) {
            throw new IllegalArgumentException("At least one keyword is required");
        }
    }

    @Override
    public GrantScope scope() {
        return GrantScope.ALL_OWN_CREATURES;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }
}

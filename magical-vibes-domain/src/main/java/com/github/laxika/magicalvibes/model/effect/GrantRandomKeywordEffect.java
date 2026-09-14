package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

/** Grants one keyword selected uniformly at random until end of turn. */
public record GrantRandomKeywordEffect(List<Keyword> options, GrantScope scope)
        implements KeywordGrantingEffect {

    public GrantRandomKeywordEffect {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("At least one keyword option is required");
        }
        if (options.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Keyword options must not contain null");
        }
        if (options.stream().distinct().count() != options.size()) {
            throw new IllegalArgumentException("Keyword options must be distinct");
        }
        if (scope != GrantScope.SELF && scope != GrantScope.TARGET) {
            throw new IllegalArgumentException("GrantRandomKeywordEffect supports only SELF and TARGET, got " + scope);
        }
        options = List.copyOf(options);
    }

    @Override
    public Set<Keyword> keywords() {
        return Set.copyOf(options);
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.TARGET
                ? TargetSpec.benign(TargetPredicates.creature())
                : new TargetSpec(null, false, null, true, 1);
    }
}

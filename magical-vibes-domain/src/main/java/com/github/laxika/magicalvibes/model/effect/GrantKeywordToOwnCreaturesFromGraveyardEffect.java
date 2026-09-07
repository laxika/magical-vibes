package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;
import java.util.Set;

/** Grants keywords to the source card's controller's creatures while the source is in a graveyard. */
public record GrantKeywordToOwnCreaturesFromGraveyardEffect(Set<Keyword> keywords, Condition condition)
        implements KeywordGrantingEffect, GraveyardStaticEffect {

    public GrantKeywordToOwnCreaturesFromGraveyardEffect(Keyword keyword, Condition condition) {
        this(Set.of(keyword), condition);
    }

    public GrantKeywordToOwnCreaturesFromGraveyardEffect {
        keywords = Set.copyOf(keywords);
        Objects.requireNonNull(condition, "condition");
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

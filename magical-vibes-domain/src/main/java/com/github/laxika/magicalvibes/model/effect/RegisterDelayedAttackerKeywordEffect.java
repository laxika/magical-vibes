package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/** Registers a delayed trigger that grants keywords to each creature that attacks this turn. */
public record RegisterDelayedAttackerKeywordEffect(Set<Keyword> keywords) implements KeywordGrantingEffect {

    public RegisterDelayedAttackerKeywordEffect {
        keywords = Set.copyOf(keywords);
    }

    @Override
    public GrantScope scope() {
        return GrantScope.TRIGGERING_PERMANENT;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }
}

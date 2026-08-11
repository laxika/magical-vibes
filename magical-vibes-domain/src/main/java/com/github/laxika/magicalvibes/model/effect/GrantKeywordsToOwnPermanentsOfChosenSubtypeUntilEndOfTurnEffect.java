package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * On resolution, prompts the controller for a creature type and grants the given keywords until
 * end of turn to other permanents they control of that type.
 */
public record GrantKeywordsToOwnPermanentsOfChosenSubtypeUntilEndOfTurnEffect(Set<Keyword> keywords)
        implements KeywordGrantingEffect {

    public GrantKeywordsToOwnPermanentsOfChosenSubtypeUntilEndOfTurnEffect {
        keywords = Set.copyOf(keywords);
    }

    @Override
    public GrantScope scope() {
        return GrantScope.OWN_PERMANENTS;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }
}

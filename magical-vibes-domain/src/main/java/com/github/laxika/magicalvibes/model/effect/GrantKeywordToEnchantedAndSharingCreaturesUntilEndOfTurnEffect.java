package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Grants keyword(s) to the enchanted creature and every creature sharing a creature type with it
 * until end of turn.
 */
public record GrantKeywordToEnchantedAndSharingCreaturesUntilEndOfTurnEffect(Set<Keyword> keywords)
        implements KeywordGrantingEffect {

    public GrantKeywordToEnchantedAndSharingCreaturesUntilEndOfTurnEffect(Keyword keyword) {
        this(Set.of(keyword));
    }

    public GrantKeywordToEnchantedAndSharingCreaturesUntilEndOfTurnEffect {
        keywords = Set.copyOf(keywords);
    }

    @Override
    public GrantScope scope() {
        return GrantScope.ENCHANTED_CREATURE;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return true;
    }
}

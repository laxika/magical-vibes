package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Creates one independent payment choice for each matching attacking creature. If its controller
 * declines the payment, the configured keyword is granted to the ability controller's creatures
 * currently blocking that attacker until end of turn.
 *
 * @param keyword the keyword granted to the matching blockers
 * @param attackerFilter the attacking creatures receiving payment choices
 * @param manaCost the mana cost each attacking creature's controller may pay
 */
public record GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect(
        Keyword keyword,
        PermanentPredicate attackerFilter,
        String manaCost
) implements CardEffect, KeywordGrantingEffect {

    @Override
    public Set<Keyword> keywords() {
        return Set.of(keyword);
    }

    @Override
    public GrantScope scope() {
        return GrantScope.TARGET;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }
}

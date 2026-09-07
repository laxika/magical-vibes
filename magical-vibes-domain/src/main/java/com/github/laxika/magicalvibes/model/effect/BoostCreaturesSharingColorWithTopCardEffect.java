package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Static boost for creatures controlled by the source's controller that share a color with their
 * controller's top library card, when that card is a creature card.
 */
public record BoostCreaturesSharingColorWithTopCardEffect(int powerBoost, int toughnessBoost)
        implements StaticCreatureBoostEffect {

    @Override
    public Set<Keyword> grantedKeywords() {
        return Set.of();
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

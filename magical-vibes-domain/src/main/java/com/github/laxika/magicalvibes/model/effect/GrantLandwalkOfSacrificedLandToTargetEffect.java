package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Target creature gains landwalk of each of the land types of the land sacrificed to pay this
 * ability's cost, until end of turn (Excavator).
 *
 * <p>The land is already in the graveyard when this resolves, so the handler reads the card
 * recorded on the source permanent at cost payment (the same channel
 * {@link AwardManaOfTypeSacrificedLandCouldProduceEffect} uses) and maps each basic land subtype
 * to its landwalk {@link com.github.laxika.magicalvibes.model.Keyword}.</p>
 */
public record GrantLandwalkOfSacrificedLandToTargetEffect() implements KeywordGrantingEffect {

    /**
     * The landwalk keywords this effect may grant. Which of them actually land depends on the
     * sacrificed land, so consumers see the full candidate set.
     */
    @Override
    public Set<Keyword> keywords() {
        return Keyword.LANDWALK_MAP.keySet();
    }

    @Override
    public GrantScope scope() {
        return GrantScope.TARGET;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

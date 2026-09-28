package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles a targeted card from any graveyard, then resolves the first matching branch, the second
 * matching branch when present, or the fallback branch based on the exiled card's characteristics.
 */
public record ExileTargetCardFromGraveyardWithConditionalEffectsEffect(
        CardPredicate matchPredicate,
        CardEffect matchingEffect,
        CardPredicate secondaryMatchPredicate,
        CardEffect secondaryMatchingEffect,
        CardEffect nonMatchingEffect
) implements CardEffect {

    public ExileTargetCardFromGraveyardWithConditionalEffectsEffect(
            CardPredicate matchPredicate,
            CardEffect matchingEffect,
            CardEffect nonMatchingEffect) {
        this(matchPredicate, matchingEffect, null, null, nonMatchingEffect);
    }

    public ExileTargetCardFromGraveyardWithConditionalEffectsEffect {
        if (matchPredicate == null) {
            throw new IllegalArgumentException("ExileTargetCardFromGraveyardWithConditionalEffectsEffect requires a predicate");
        }
        if (matchingEffect == null || nonMatchingEffect == null) {
            throw new IllegalArgumentException("Both conditional effects are required");
        }
        if ((secondaryMatchPredicate == null) != (secondaryMatchingEffect == null)) {
            throw new IllegalArgumentException("Both secondary conditional fields are required together");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}

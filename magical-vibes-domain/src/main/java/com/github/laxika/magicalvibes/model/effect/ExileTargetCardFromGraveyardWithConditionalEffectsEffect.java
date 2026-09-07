package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles a targeted card from any graveyard, then resolves one of two effects based on the
 * exiled card's characteristics.
 */
public record ExileTargetCardFromGraveyardWithConditionalEffectsEffect(
        CardPredicate matchPredicate,
        CardEffect matchingEffect,
        CardEffect nonMatchingEffect
) implements CardEffect {

    public ExileTargetCardFromGraveyardWithConditionalEffectsEffect {
        if (matchPredicate == null) {
            throw new IllegalArgumentException("ExileTargetCardFromGraveyardWithConditionalEffectsEffect requires a predicate");
        }
        if (matchingEffect == null || nonMatchingEffect == null) {
            throw new IllegalArgumentException("Both conditional effects are required");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}

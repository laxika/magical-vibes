package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

/**
 * Returns up to one target card matching each supplied filter from the controller's graveyard to
 * their hand. The filters represent separate target groups, so a card can satisfy only one group
 * in a single selection.
 *
 * <p>This effect uses the spell's multi-card graveyard targeting flow. The selected cards are
 * stored on the stack entry and moved by the normal graveyard return support at resolution.</p>
 *
 * @param filters the ordered, independently optional target-group filters
 */
public record ReturnUpToOneOfEachFilterFromGraveyardToHandEffect(List<CardPredicate> filters)
        implements IndependentlyTargetedGraveyardCardsEffect {

    public ReturnUpToOneOfEachFilterFromGraveyardToHandEffect {
        filters = List.copyOf(filters);
    }

    @Override
    public List<CardPredicate> targetFilters() {
        return filters;
    }

    @Override
    public List<String> targetDescriptions() {
        return filters.stream().map(CardPredicateUtils::describeFilter).toList();
    }

    @Override
    public boolean requiresDistinctTargets() {
        return true;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles a targeted graveyard card, then lets the effect controller play it for as long as it
 * remains exiled.
 *
 * @param filter           predicate restricting valid graveyard targets; {@code null} means any card
 * @param ownGraveyardOnly when {@code true}, only the controller's graveyard can be targeted
 */
public record ExileTargetCardFromGraveyardMayPlayWhileExiledEffect(
        CardPredicate filter,
        boolean ownGraveyardOnly
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        GraveyardSearchScope scope = ownGraveyardOnly
                ? GraveyardSearchScope.CONTROLLERS_GRAVEYARD
                : GraveyardSearchScope.ALL_GRAVEYARDS;
        return TargetSpec.benign(filter == null
                ? TargetPredicates.graveyardCard(scope)
                : TargetPredicates.graveyardCards(filter, scope));
    }
}

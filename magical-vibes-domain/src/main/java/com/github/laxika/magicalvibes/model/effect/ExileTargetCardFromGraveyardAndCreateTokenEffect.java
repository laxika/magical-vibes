package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Exiles a target graveyard card and creates the supplied token under the effect controller's control. */
public record ExileTargetCardFromGraveyardAndCreateTokenEffect(
        CardPredicate filter,
        boolean ownGraveyardOnly,
        CreateTokenEffect tokenTemplate,
        boolean trackWithSource
) implements TokenCreatingEffect {

    @Override
    public TargetSpec targetSpec() {
        GraveyardSearchScope scope = ownGraveyardOnly
                ? GraveyardSearchScope.CONTROLLERS_GRAVEYARD : GraveyardSearchScope.ALL_GRAVEYARDS;
        return TargetSpec.benign(filter == null
                ? TargetPredicates.graveyardCard(scope)
                : TargetPredicates.graveyardCards(filter, scope));
    }

    @Override
    public DynamicAmount tokenAmount() {
        return tokenTemplate.amount();
    }

    @Override
    public CardType tokenType() {
        return tokenTemplate.primaryType();
    }

    @Override
    public int tokenPower() {
        return tokenTemplate.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return tokenTemplate.tokenToughness();
    }
}

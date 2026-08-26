package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile target card from a graveyard. If it was a creature card, create the supplied token.
 * An optional filter can narrow the target, as needed for wordings that say "target creature card".
 *
 * @param filter predicate restricting valid targets; {@code null} allows any card
 * @param token token created when the exiled card is a creature
 */
public record ExileGraveyardCardCreateTokenIfCreatureEffect(CardPredicate filter, CreateTokenEffect token)
        implements CardEffect, TokenCreatingEffect {

    public ExileGraveyardCardCreateTokenIfCreatureEffect() {
        this(null, CreateTokenEffect.blackZombie(1));
    }

    public ExileGraveyardCardCreateTokenIfCreatureEffect(CardPredicate filter) {
        this(filter, CreateTokenEffect.blackZombie(1));
    }

    public ExileGraveyardCardCreateTokenIfCreatureEffect(CreateTokenEffect token) {
        this(null, token);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(filter == null
                ? TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS)
                : TargetPredicates.graveyardCards(filter, GraveyardSearchScope.ALL_GRAVEYARDS));
    }

    @Override
    public DynamicAmount tokenAmount() {
        return token.amount();
    }

    @Override
    public CardType tokenType() {
        return token.primaryType();
    }

    @Override
    public int tokenPower() {
        return token.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return token.tokenToughness();
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile target card from a graveyard. If it was a creature card, create one token from the
 * supplied template. An optional filter can narrow the target, as needed for wordings that say
 * "target creature card".
 *
 * @param filter predicate restricting valid targets; {@code null} allows any card
 * @param graveyardScope graveyard from which the target may be chosen
 * @param tokenTemplate token created when the exiled card was a creature
 */
public record ExileGraveyardCardCreateTokenIfCreatureEffect(
        CardPredicate filter,
        GraveyardSearchScope graveyardScope,
        CreateTokenEffect tokenTemplate
)
        implements CardEffect, TokenCreatingEffect {

    public ExileGraveyardCardCreateTokenIfCreatureEffect() {
        this(null, GraveyardSearchScope.ALL_GRAVEYARDS, CreateTokenEffect.blackZombie(1));
    }

    public ExileGraveyardCardCreateTokenIfCreatureEffect(CardPredicate filter) {
        this(filter, GraveyardSearchScope.ALL_GRAVEYARDS, CreateTokenEffect.blackZombie(1));
    }

    public ExileGraveyardCardCreateTokenIfCreatureEffect(GraveyardSearchScope graveyardScope,
                                                         CreateTokenEffect tokenTemplate) {
        this(null, graveyardScope, tokenTemplate);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(filter == null
                ? TargetPredicates.graveyardCard(graveyardScope)
                : TargetPredicates.graveyardCards(filter, graveyardScope));
    }

    @Override
    public DynamicAmount tokenAmount() {
        return new Fixed(1);
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

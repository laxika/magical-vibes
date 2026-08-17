package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile target card from a graveyard. If it was a creature card, create a 2/2 black Zombie
 * creature token. Used by Deluge of the Dead. An optional filter can narrow the target, as needed
 * for wordings that say "target creature card".
 *
 * @param filter predicate restricting valid targets; {@code null} allows any card
 */
public record ExileGraveyardCardCreateTokenIfCreatureEffect(CardPredicate filter)
        implements CardEffect, TokenCreatingEffect {

    public ExileGraveyardCardCreateTokenIfCreatureEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(filter == null
                ? TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS)
                : TargetPredicates.graveyardCards(filter, GraveyardSearchScope.ALL_GRAVEYARDS));
    }

    @Override
    public DynamicAmount tokenAmount() {
        return new Fixed(1);
    }

    @Override
    public CardType tokenType() {
        return CardType.CREATURE;
    }

    @Override
    public int tokenPower() {
        return 2;
    }

    @Override
    public int tokenToughness() {
        return 2;
    }
}

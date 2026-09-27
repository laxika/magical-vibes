package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Exiles a target creature card from a graveyard and creates one token, optionally entering with
 * +1/+1 counters when the exiled card's mana value reaches the configured threshold.
 */
public record ExileTargetCreatureCardFromGraveyardCreateTokenWithManaValueCounterEffect(
        CreateTokenEffect tokenTemplate,
        int minimumManaValue,
        int counterAmount,
        GraveyardSearchScope graveyardScope
) implements TokenCreatingEffect {

    public ExileTargetCreatureCardFromGraveyardCreateTokenWithManaValueCounterEffect(
            CreateTokenEffect tokenTemplate, int minimumManaValue) {
        this(tokenTemplate, minimumManaValue, 1, GraveyardSearchScope.ALL_GRAVEYARDS);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE), graveyardScope));
    }

    @Override
    public com.github.laxika.magicalvibes.model.amount.DynamicAmount tokenAmount() {
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

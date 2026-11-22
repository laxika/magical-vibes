package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Creates one token attached to each creature matching {@code filter} that one of {@code controllers} controls ("for
 * each Rat you control", "for each creature your opponents control"), under the effect's controller's control.
 */
public record CreateTokensAttachedToControlledCreaturesEffect(
        CreateTokenEffect token,
        PermanentPredicate filter,
        PlayerSubject controllers
) implements CardEffect, TokenCreatingEffect {

    public CreateTokensAttachedToControlledCreaturesEffect(CreateTokenEffect token, PermanentPredicate filter) {
        this(token, filter, Subjects.you());
    }

    @Override
    public DynamicAmount tokenAmount() {
        return new PermanentCount(filter,
                controllers instanceof PlayerSubject.You ? CountScope.CONTROLLER : CountScope.OPPONENTS);
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

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * "Create a token, then put [amount] counters on that token." The counters are put on the tokens
 * created by this same resolution, so no separate trigger goes on the stack for them. The amount is
 * evaluated when the effect resolves.
 *
 * @param tokenTemplate the token to create
 * @param counterType   the kind of counter to put on each created token
 * @param amount        how many counters each created token receives
 */
public record CreateTokenThenPutCountersEffect(
        CreateTokenEffect tokenTemplate,
        CounterType counterType,
        DynamicAmount amount
) implements TokenCreatingEffect {

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

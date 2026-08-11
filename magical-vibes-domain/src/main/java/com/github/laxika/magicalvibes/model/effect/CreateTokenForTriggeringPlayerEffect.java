package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Creates the wrapped token under the control of the player whose action caused the trigger.
 *
 * <p>The trigger collector carries that player on the stack entry's non-targeting target slot.
 * In a spell-cast trigger, the collector also stores the triggering spell's mana value in the
 * entry event value, so the token blueprint may use {@code EventValue} for its power and toughness.
 *
 * @param token token blueprint to create
 */
public record CreateTokenForTriggeringPlayerEffect(CreateTokenEffect token)
        implements TokenCreatingEffect {

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

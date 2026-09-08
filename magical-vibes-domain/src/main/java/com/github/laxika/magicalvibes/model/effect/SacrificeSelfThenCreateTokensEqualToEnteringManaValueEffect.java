package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/**
 * Trigger-only effect for an entering-creature trigger that sacrifices its source and then
 * creates token copies based on the entering creature's mana value.
 *
 * <p>The trigger collector snapshots that mana value on the stack entry. The normal effect handler
 * materializes the token blueprint and reuses {@link SacrificeSelfThenEffect} for the sacrifice
 * and conditional follow-up.
 */
public record SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect(CreateTokenEffect tokenTemplate)
        implements TokenCreatingEffect {

    public SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect {
        if (tokenTemplate == null) {
            throw new IllegalArgumentException("Token template is required");
        }
    }

    @Override
    public DynamicAmount tokenAmount() {
        return new EventValue();
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

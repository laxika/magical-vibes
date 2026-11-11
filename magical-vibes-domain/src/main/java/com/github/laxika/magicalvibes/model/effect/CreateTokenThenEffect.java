package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Creates the wrapped tokens and, if at least one token was created, puts the follow-up effect on
 * the stack as a reflexive triggered ability ("create a token. When you do, ...", CR 603.12).
 *
 * @param tokenEffect  the tokens created
 * @param thenEffect   the effect of the reflexive ability
 * @param aboutThatToken whether the reflexive ability is about "that token" ("... attach it to that token"): one
 *                     ability for each token created, a replacement effect having made more than one, with the
 *                     token as the permanent the ability's effect acts for (its source permanent)
 */
public record CreateTokenThenEffect(CreateTokenEffect tokenEffect, CardEffect thenEffect, boolean aboutThatToken)
        implements TokenCreatingEffect {

    public CreateTokenThenEffect(CreateTokenEffect tokenEffect, CardEffect thenEffect) {
        this(tokenEffect, thenEffect, false);
    }

    /** "Create a token. When you do, [effect about that token]": one reflexive ability for each token created. */
    public static CreateTokenThenEffect aboutThatToken(CreateTokenEffect tokenEffect, CardEffect thenEffect) {
        return new CreateTokenThenEffect(tokenEffect, thenEffect, true);
    }

    public CreateTokenThenEffect {
        if (tokenEffect == null || thenEffect == null) {
            throw new IllegalArgumentException("CreateTokenThenEffect requires both effects");
        }
    }

    @Override
    public DynamicAmount tokenAmount() {
        return tokenEffect.tokenAmount();
    }

    @Override
    public CardType tokenType() {
        return tokenEffect.tokenType();
    }

    @Override
    public int tokenPower() {
        return tokenEffect.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return tokenEffect.tokenToughness();
    }
}

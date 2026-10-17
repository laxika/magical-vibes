package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * Creates the supplied token blueprint and links every token created by the resolution to the
 * source permanent. The source's leaves-the-battlefield effect can then clean up those tokens. The
 * optional reciprocal link gives each token a reference to the source when it leaves; cards that
 * only need source tracking can disable that behavior.
 *
 * <p>{@code leavesBattlefieldEffect}, when set, is registered as a delayed triggered ability for each
 * created token: "When that token leaves the battlefield, [effect]". It is controlled by the ability's
 * controller, has the ability's source as its source, and does not depend on the token's own
 * abilities (CR 603.7).
 *
 * <p>{@code sourceLeavesBattlefieldEffect}, when set, is registered as one delayed triggered ability on
 * the source: "Exile those tokens when this creature leaves the battlefield". It only exists once this
 * effect resolves, so a source that already left has nothing to trigger, and it does not depend on the
 * source's own abilities (CR 603.7).
 */
public record CreateTokenAndLinkToSourceEffect(CreateTokenEffect token, boolean linkTokenToSource,
                                               CardEffect leavesBattlefieldEffect,
                                               CardEffect sourceLeavesBattlefieldEffect)
        implements TokenCreatingEffect {

    public CreateTokenAndLinkToSourceEffect(CreateTokenEffect token) {
        this(token, true, null, null);
    }

    public CreateTokenAndLinkToSourceEffect(CreateTokenEffect token, boolean linkTokenToSource) {
        this(token, linkTokenToSource, null, null);
    }

    public CreateTokenAndLinkToSourceEffect(CreateTokenEffect token, boolean linkTokenToSource,
                                            CardEffect leavesBattlefieldEffect) {
        this(token, linkTokenToSource, leavesBattlefieldEffect, null);
    }

    public CreateTokenAndLinkToSourceEffect {
        Objects.requireNonNull(token, "token");
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

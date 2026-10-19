package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Tempting offer that offers each opponent the same token creation. Opponents decide in turn order;
 * once every opponent has decided, each opponent who accepted creates their tokens and the ability
 * controller creates another batch for each of them.
 *
 * <p>{@code acceptedOpponentIds} records the opponents who have accepted so far, so their batches
 * are created together after the last decision rather than as each opponent answers.</p>
 */
public record TemptingOfferCreateTokensEffect(
        CreateTokenEffect tokenEffect,
        List<UUID> remainingOpponentIds,
        List<UUID> acceptedOpponentIds,
        UUID abilityControllerId,
        boolean controllerCreatesFirst
) implements TokenCreatingEffect {

    public TemptingOfferCreateTokensEffect {
        Objects.requireNonNull(tokenEffect, "tokenEffect is required");
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
        acceptedOpponentIds = acceptedOpponentIds == null ? List.of() : List.copyOf(acceptedOpponentIds);
    }

    public TemptingOfferCreateTokensEffect(CreateTokenEffect tokenEffect) {
        this(tokenEffect, null, List.of(), null, true);
    }

    public TemptingOfferCreateTokensEffect(CreateTokenEffect tokenEffect,
                                           boolean controllerCreatesFirst) {
        this(tokenEffect, null, List.of(), null, controllerCreatesFirst);
    }

    @Override
    public DynamicAmount tokenAmount() {
        return tokenEffect.amount();
    }

    @Override
    public CardType tokenType() {
        return tokenEffect.primaryType();
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

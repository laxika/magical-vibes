package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.List;
import java.util.Objects;

/** Creates tokens, then lets the controller choose one listed counter for each created token. */
public record CreateTokensWithChosenCountersEffect(CreateTokenEffect tokenTemplate,
                                                    List<CounterType> counterTypes)
        implements TokenCreatingEffect {

    public CreateTokensWithChosenCountersEffect {
        Objects.requireNonNull(tokenTemplate, "tokenTemplate");
        counterTypes = List.copyOf(counterTypes);
        if (counterTypes.isEmpty()) {
            throw new IllegalArgumentException("At least one counter type is required");
        }
        if (counterTypes.stream().anyMatch(type -> type == CounterType.ANY || type == CounterType.SILVER)) {
            throw new IllegalArgumentException("Counter choices must be concrete counter types");
        }
    }

    @Override
    public DynamicAmount tokenAmount() {
        return tokenTemplate.tokenAmount();
    }

    @Override
    public CardType tokenType() {
        return tokenTemplate.tokenType();
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

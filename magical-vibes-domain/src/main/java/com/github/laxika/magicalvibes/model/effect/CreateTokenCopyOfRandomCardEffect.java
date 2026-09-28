package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Creates a token copy of one card selected uniformly at random from fixed card factories. */
public record CreateTokenCopyOfRandomCardEffect(List<Supplier<? extends Card>> cardFactories)
        implements CardEffect {

    public CreateTokenCopyOfRandomCardEffect {
        Objects.requireNonNull(cardFactories, "Card factories cannot be null");
        if (cardFactories.isEmpty() || cardFactories.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("At least one non-null card factory is required");
        }
        cardFactories = List.copyOf(cardFactories);
    }
}

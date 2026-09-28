package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Creates a copy of one fixed card selected uniformly at random and offers it for free casting. */
public record CreateRandomCardCopyAndMayCastEffect(List<Supplier<? extends Card>> cardFactories)
        implements CardEffect {

    public CreateRandomCardCopyAndMayCastEffect {
        Objects.requireNonNull(cardFactories, "Card factories cannot be null");
        if (cardFactories.isEmpty() || cardFactories.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("At least one non-null card factory is required");
        }
        cardFactories = List.copyOf(cardFactories);
    }
}

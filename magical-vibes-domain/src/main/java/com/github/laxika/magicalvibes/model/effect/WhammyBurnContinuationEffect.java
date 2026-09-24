package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import java.util.List;
import java.util.Objects;

/** Pending choice state for the next reveal of a Whammy Burn whammy deck. */
public record WhammyBurnContinuationEffect(List<CardSubtype> remainingCards, int revealedCount)
        implements CardEffect {

    public WhammyBurnContinuationEffect {
        Objects.requireNonNull(remainingCards, "remainingCards");
        if (revealedCount < 1) {
            throw new IllegalArgumentException("revealedCount must be positive");
        }
        remainingCards = List.copyOf(remainingCards);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}

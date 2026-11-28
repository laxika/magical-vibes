package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;

import java.util.List;
import java.util.Objects;

/** Creates a copy of a card of one of {@code printings}, picked uniformly at random, and offers it for free casting. */
public record CreateRandomCardCopyAndMayCastEffect(List<SpellbookCard> printings) implements CardEffect {

    public CreateRandomCardCopyAndMayCastEffect {
        Objects.requireNonNull(printings, "printings");
        if (printings.isEmpty() || printings.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("At least one non-null printing is required");
        }
        printings = List.copyOf(printings);
    }
}

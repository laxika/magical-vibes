package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Looks at the top two cards and combines them into a modal double-faced card.
 * The card IDs are empty for the initial library look and populated for the
 * follow-up after the front face has been chosen.
 */
public record CreateModalDoubleFacedCardFromTopTwoEffect(List<UUID> cardIds) implements CardEffect {

    public CreateModalDoubleFacedCardFromTopTwoEffect() {
        this(List.of());
    }

    public CreateModalDoubleFacedCardFromTopTwoEffect {
        cardIds = List.copyOf(cardIds);
    }
}

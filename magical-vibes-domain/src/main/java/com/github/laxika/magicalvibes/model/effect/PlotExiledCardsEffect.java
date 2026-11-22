package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "It becomes plotted" after an exile step: each card {@code cards} names in exile becomes plotted, so its owner may
 * cast it on a later turn as a sorcery without paying its mana cost.
 */
public record PlotExiledCardsEffect(CardSubject cards) implements CardEffect {

    public PlotExiledCardsEffect {
        Objects.requireNonNull(cards, "cards");
        if (cards.declaresTarget()) {
            throw new IllegalArgumentException("The step names the cards an earlier step moved: " + cards);
        }
    }
}

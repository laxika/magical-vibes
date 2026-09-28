package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

/** Resolves one of Druid of the Emerald Grove's d20 basic-land destinations. */
public record D20BasicLandPlacementEffect(Placement placement, List<Card> cards) implements CardEffect {

    public enum Placement {
        TO_HAND,
        ONE_TO_BATTLEFIELD_TAPPED,
        ALL_TO_BATTLEFIELD_TAPPED
    }

    public D20BasicLandPlacementEffect {
        cards = List.copyOf(cards);
    }

    public D20BasicLandPlacementEffect(Placement placement) {
        this(placement, List.of());
    }

    public D20BasicLandPlacementEffect withCards(List<Card> selectedCards) {
        return new D20BasicLandPlacementEffect(placement, selectedCards);
    }
}

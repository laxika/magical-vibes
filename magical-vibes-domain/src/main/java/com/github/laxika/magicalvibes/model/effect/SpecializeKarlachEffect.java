package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Applies one of Karlach, Raging Tiefling's digital specialized faces. {@code returnFromGraveyard}
 * marks the triggered ability "When this card specializes from your graveyard, return it from your
 * graveyard to the battlefield", which resolves after the card has already specialized in the graveyard.
 */
public record SpecializeKarlachEffect(CardColor color, boolean returnFromGraveyard) implements CardEffect {

    public SpecializeKarlachEffect(CardColor color) {
        this(color, false);
    }
}

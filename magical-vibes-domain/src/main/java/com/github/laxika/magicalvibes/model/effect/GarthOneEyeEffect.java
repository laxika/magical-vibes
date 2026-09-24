package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

/** Resolution-time choice of an unchosen named card for Garth One-Eye. */
public record GarthOneEyeEffect(List<Card> cards) implements CardEffect {

    public GarthOneEyeEffect {
        cards = List.copyOf(cards);
    }
}

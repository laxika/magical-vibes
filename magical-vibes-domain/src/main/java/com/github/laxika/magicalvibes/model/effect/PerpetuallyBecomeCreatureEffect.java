package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** Perpetually makes the source a creature while retaining its existing card types. */
public record PerpetuallyBecomeCreatureEffect(int power, int toughness, CardSubtype subtype)
        implements CardEffect {
}

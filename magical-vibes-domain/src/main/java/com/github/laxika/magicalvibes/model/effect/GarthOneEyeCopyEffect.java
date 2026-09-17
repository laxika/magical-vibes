package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Creates and offers the normal-cost cast of Garth One-Eye's selected card copy. */
public record GarthOneEyeCopyEffect(Card card) implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Perpetually transforms Klement into the selected digital specialized face. */
public record SpecializeKlementEffect(CardColor color) implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Applies one of Rasaad, Monk of Selûne's five specialized faces. */
public record SpecializeRasaadEffect(CardColor color) implements CardEffect {
}

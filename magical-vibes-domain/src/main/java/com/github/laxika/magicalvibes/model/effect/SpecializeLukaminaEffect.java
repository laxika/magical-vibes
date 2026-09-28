package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Applies one of Lukamina, Moon Druid's five digital specialized faces. */
public record SpecializeLukaminaEffect(CardColor color) implements CardEffect {
}

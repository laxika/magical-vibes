package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Transforms Sarevok the Usurper into the selected digital specialized face. */
public record SpecializeSarevokEffect(CardColor color) implements CardEffect {
}

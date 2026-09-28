package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Applies one of Lae'zel, Githyanki Warrior's five specialized faces. */
public record SpecializeLaezelEffect(CardColor color) implements CardEffect {
}

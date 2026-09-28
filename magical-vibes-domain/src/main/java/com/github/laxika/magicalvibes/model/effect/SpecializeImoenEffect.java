package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Applies one of Imoen, Trickster Friend's five specialized faces. */
public record SpecializeImoenEffect(CardColor color) implements CardEffect {
}

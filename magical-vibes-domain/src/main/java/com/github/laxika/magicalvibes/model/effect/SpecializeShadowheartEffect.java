package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Transforms Shadowheart, Sharran Cleric into the selected digital specialized face. */
public record SpecializeShadowheartEffect(CardColor color) implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

public record BoostNonColorCreaturesEffect(CardColor excludedColor, int powerBoost, int toughnessBoost) implements CardEffect {
}

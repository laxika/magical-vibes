package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

public record BoostOtherCreaturesByColorEffect(CardColor color, int powerBoost, int toughnessBoost) implements CardEffect {
}

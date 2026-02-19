package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

public record DiscardCardTypeCost(CardType requiredType) implements CardEffect {
}

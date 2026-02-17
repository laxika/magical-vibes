package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

public record SacrificeUnlessDiscardCardTypeEffect(CardType requiredType) implements CardEffect {
}

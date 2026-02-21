package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

public record PutCardToBattlefieldEffect(CardType cardType) implements CardEffect {
}

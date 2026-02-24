package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardType;

public record CardTypePredicate(CardType cardType) implements CardPredicate {
}

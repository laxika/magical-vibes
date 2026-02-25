package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardColor;

public record CardColorPredicate(CardColor color) implements CardPredicate {
}

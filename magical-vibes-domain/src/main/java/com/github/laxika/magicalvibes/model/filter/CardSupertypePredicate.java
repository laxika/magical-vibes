package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardSupertype;

public record CardSupertypePredicate(CardSupertype supertype) implements CardPredicate {
}

package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardSubtype;

public record CardSubtypePredicate(CardSubtype subtype) implements CardPredicate {
}

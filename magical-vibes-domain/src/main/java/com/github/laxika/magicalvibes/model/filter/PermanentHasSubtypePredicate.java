package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardSubtype;

public record PermanentHasSubtypePredicate(CardSubtype subtype) implements PermanentPredicate {
}

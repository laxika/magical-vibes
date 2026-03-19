package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardSupertype;

public record PermanentHasSupertypePredicate(CardSupertype supertype) implements PermanentPredicate {
}

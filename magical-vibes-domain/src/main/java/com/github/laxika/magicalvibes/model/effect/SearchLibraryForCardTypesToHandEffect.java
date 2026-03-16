package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

public record SearchLibraryForCardTypesToHandEffect(CardPredicate filter) implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record ExileAllPermanentsEffect(PermanentPredicate filter) implements CardEffect {
}

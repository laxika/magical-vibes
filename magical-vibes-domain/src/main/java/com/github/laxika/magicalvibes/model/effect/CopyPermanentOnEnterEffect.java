package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel) implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record AddManaPerControlledPermanentEffect(ManaColor color, PermanentPredicate predicate, String description) implements ManaProducingEffect {
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record PutAllPermanentsOnBottomOfLibraryEffect(PermanentPredicate filter) implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Static entry replacement that makes matching permanents enter as creatures with fixed base
 * power and toughness, adding the supplied creature subtypes in addition to their other types.
 */
public record ControlledPermanentsEnterAsCreatureEffect(
        PermanentPredicate enteringPermanentPredicate,
        Integer basePower,
        Integer baseToughness,
        List<CardSubtype> addedSubtypes
) implements ControlledPermanentEntryCharacteristicsEffect {

    public ControlledPermanentsEnterAsCreatureEffect {
        addedSubtypes = List.copyOf(addedSubtypes);
    }
}

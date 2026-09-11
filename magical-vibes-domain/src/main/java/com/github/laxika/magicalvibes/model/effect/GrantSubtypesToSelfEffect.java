package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/** Static effect that grants the listed subtypes to its source in every zone. */
public record GrantSubtypesToSelfEffect(List<CardSubtype> grantedSubtypes)
        implements SelfAllZoneSubtypeGrantingEffect {

    public GrantSubtypesToSelfEffect {
        grantedSubtypes = List.copyOf(grantedSubtypes);
    }
}

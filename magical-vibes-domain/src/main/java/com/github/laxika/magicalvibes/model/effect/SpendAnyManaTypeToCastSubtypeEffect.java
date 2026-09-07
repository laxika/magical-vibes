package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Set;

/** Static permission to spend mana of any type to cast spells with the given subtype. */
public record SpendAnyManaTypeToCastSubtypeEffect(Set<CardSubtype> spellSubtypes)
        implements AnyManaTypeCastEffect {

    public SpendAnyManaTypeToCastSubtypeEffect(CardSubtype spellSubtype) {
        this(Set.of(spellSubtype));
    }
}

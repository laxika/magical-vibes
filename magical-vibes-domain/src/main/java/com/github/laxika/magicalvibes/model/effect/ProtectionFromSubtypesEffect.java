package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Set;

/**
 * Protection from sources with any of the given subtypes.
 *
 * @param subtypes            the protected-against subtypes
 * @param creatureSourcesOnly {@code true} for "protection from [subtype] creatures" — the source
 *                            must also be a creature, so a noncreature source that happens to
 *                            carry the subtype (a Tribal spell) is not protected against.
 *                            {@code false} for the printed "protection from [subtype]" shape
 *                            (Baneslayer Angel), which covers every source with the subtype.
 */
public record ProtectionFromSubtypesEffect(Set<CardSubtype> subtypes, boolean creatureSourcesOnly)
        implements ProtectionGrantingEffect {

    public ProtectionFromSubtypesEffect(Set<CardSubtype> subtypes) {
        this(subtypes, false);
    }

    @Override
    public Set<CardSubtype> protectionFromSubtypes() {
        return subtypes;
    }

    @Override
    public boolean subtypeProtectionRequiresCreatureSource() {
        return creatureSourcesOnly;
    }
}

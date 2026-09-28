package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Library ninjutsu permission on a card, and the resolution effect created when that ability is
 * activated.
 */
public record LibraryNinjutsuEffect(String manaCost, UUID attackTargetId)
        implements LibrarySearchCastPermission {

    public LibraryNinjutsuEffect(String manaCost) {
        this(manaCost, null);
    }
}

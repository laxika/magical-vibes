package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Routes one card selected by Invoke Calamity through its original-zone free-cast path.
 *
 * @param reservedManaValue  the smallest total mana value the selected spells cast after this one
 *                           can have, which this spell must leave room for
 * @param earlierSelectedIds the selected cards cast before this one; the spells they put on the
 *                           stack have already spent part of the limit
 */
public record CastInvokeCalamityChosenSpellEffect(boolean fromGraveyard, int reservedManaValue,
                                                  List<UUID> earlierSelectedIds) implements CardEffect {

    public CastInvokeCalamityChosenSpellEffect {
        earlierSelectedIds = List.copyOf(earlierSelectedIds);
    }
}

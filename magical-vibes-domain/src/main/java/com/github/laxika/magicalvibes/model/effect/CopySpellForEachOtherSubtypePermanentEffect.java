package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * Trigger descriptor for {@code ON_ANY_PLAYER_CASTS_SPELL}: whenever a player casts an
 * instant or sorcery spell that targets only a single permanent with the given subtype,
 * that player copies the spell for each other permanent with that subtype the spell could
 * target. Each copy targets a different one of those permanents.
 * <p>
 * Card-definition form uses the single-arg constructor ({@code spellSnapshot == null}).
 * At trigger time, {@code checkSpellCastTriggers} populates the snapshot fields and
 * places the resolution form on the stack.
 * <p>
 * Used by Precursor Golem.
 */
public record CopySpellForEachOtherSubtypePermanentEffect(
        CardSubtype subtype,
        StackEntry spellSnapshot,
        UUID castingPlayerId,
        UUID originalTargetPermanentId
) implements CardEffect {

    /** Card definition constructor — trigger marker only. */
    public CopySpellForEachOtherSubtypePermanentEffect(CardSubtype subtype) {
        this(subtype, null, null, null);
    }
}

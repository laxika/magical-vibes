package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * Trigger descriptor for {@code ON_ANY_PLAYER_CASTS_SPELL}: whenever a player casts an
 * instant or sorcery spell that targets only the source creature, that player copies the
 * spell for each other creature they control that the spell could target. Each copy
 * targets a different one of those creatures.
 * <p>
 * Card-definition form uses the no-arg constructor ({@code spellSnapshot == null}).
 * At trigger time, {@code checkSpellCastTriggers} populates the snapshot fields and
 * places the resolution form on the stack.
 * <p>
 * Used by Mirrorwing Dragon.
 */
public record CopySpellForEachOtherControlledCreatureEffect(
        StackEntry spellSnapshot,
        UUID castingPlayerId,
        UUID originalTargetId
) implements CardEffect {

    /** Card definition constructor — trigger marker only. */
    public CopySpellForEachOtherControlledCreatureEffect() {
        this(null, null, null);
    }
}

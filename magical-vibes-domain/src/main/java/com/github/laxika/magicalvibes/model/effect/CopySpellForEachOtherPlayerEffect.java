package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * Trigger descriptor for {@code ON_ANY_PLAYER_CASTS_SPELL}: whenever a player casts an
 * instant or sorcery spell, each other player copies that spell. Each of those players
 * may choose new targets for their copy.
 * <p>
 * Card-definition form uses the no-arg constructor ({@code spellSnapshot == null}).
 * At trigger time, {@code checkSpellCastTriggers} populates the snapshot fields and
 * places the resolution form on the stack.
 * <p>
 * Used by Hive Mind.
 */
public record CopySpellForEachOtherPlayerEffect(
        StackEntry spellSnapshot,
        UUID castingPlayerId
) implements CardEffect {

    /** Card definition constructor — trigger marker only. */
    public CopySpellForEachOtherPlayerEffect() {
        this(null, null);
    }
}

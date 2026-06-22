package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

import java.util.UUID;

/**
 * Trigger descriptor for {@code ON_ANY_PLAYER_CASTS_SPELL}: whenever a player casts a spell
 * matching {@code spellFilter}, each other player copies that spell. Each of those players may
 * choose new targets for their copy.
 * <p>
 * When {@code optional} is {@code false} the copy is mandatory (Hive Mind). When
 * {@code optional} is {@code true} each other player <em>may</em> copy the spell
 * (Curse of Echoes).
 * <p>
 * {@code spellFilter} is evaluated against the cast spell's stack entry and fully expresses
 * what triggers the copy — e.g. a {@code StackEntryTypeInPredicate} restricting to instants and
 * sorceries (both cards), optionally combined with a
 * {@code StackEntryControlledByEnchantedPlayerPredicate} so Curse of Echoes only fires when the
 * enchanted player is the caster.
 * <p>
 * Card-definition form leaves {@code spellSnapshot == null}. At trigger time,
 * {@code checkSpellCastTriggers} populates the snapshot fields and places the resolution form on
 * the stack.
 * <p>
 * Used by Hive Mind (mandatory) and Curse of Echoes (optional, enchanted-player filtered).
 */
public record CopySpellForEachOtherPlayerEffect(
        StackEntry spellSnapshot,
        UUID castingPlayerId,
        boolean optional,
        StackEntryPredicate spellFilter
) implements CardEffect {

    /** Card-definition constructor — mandatory copy filtered by {@code spellFilter} (Hive Mind). */
    public CopySpellForEachOtherPlayerEffect(StackEntryPredicate spellFilter) {
        this(null, null, false, spellFilter);
    }

    /** Card-definition constructor — optional copy filtered by {@code spellFilter} (Curse of Echoes). */
    public CopySpellForEachOtherPlayerEffect(boolean optional, StackEntryPredicate spellFilter) {
        this(null, null, optional, spellFilter);
    }

    /** Resolution form for a mandatory copy. */
    public CopySpellForEachOtherPlayerEffect(StackEntry spellSnapshot, UUID castingPlayerId) {
        this(spellSnapshot, castingPlayerId, false, null);
    }

    /** Resolution form carrying the mandatory/optional mode. */
    public CopySpellForEachOtherPlayerEffect(StackEntry spellSnapshot, UUID castingPlayerId, boolean optional) {
        this(spellSnapshot, castingPlayerId, optional, null);
    }
}

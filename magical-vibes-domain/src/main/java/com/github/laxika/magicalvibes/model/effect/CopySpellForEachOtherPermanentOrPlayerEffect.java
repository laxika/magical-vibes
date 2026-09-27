package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * Copies an instant or sorcery spell for each other legal permanent, player, spell, or card
 * outside the battlefield. The empty form is a spell effect; the populated form is a resolved
 * spell-cast trigger snapshot.
 */
public record CopySpellForEachOtherPermanentOrPlayerEffect(
        StackEntry spellSnapshot,
        UUID castingPlayerId,
        UUID originalTargetId,
        boolean permanentOrPlayerOnly
) implements CardEffect {

    public CopySpellForEachOtherPermanentOrPlayerEffect() {
        this(null, null, null, false);
    }

    /** Card-definition form for effects whose copies may target only permanents or players. */
    public static CopySpellForEachOtherPermanentOrPlayerEffect permanentsAndPlayersOnly() {
        return new CopySpellForEachOtherPermanentOrPlayerEffect(null, null, null, true);
    }

    /** Spell-cast trigger snapshot form. */
    public CopySpellForEachOtherPermanentOrPlayerEffect(StackEntry spellSnapshot,
                                                        UUID castingPlayerId,
                                                        UUID originalTargetId) {
        this(spellSnapshot, castingPlayerId, originalTargetId, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return spellSnapshot == null
                ? TargetSpec.benign(TargetPredicates.spellOnStack())
                : TargetSpec.NONE;
    }
}

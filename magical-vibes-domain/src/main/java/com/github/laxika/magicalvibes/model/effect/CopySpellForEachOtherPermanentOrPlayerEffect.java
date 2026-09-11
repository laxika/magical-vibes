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
        UUID originalTargetId
) implements CardEffect {

    public CopySpellForEachOtherPermanentOrPlayerEffect() {
        this(null, null, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return spellSnapshot == null
                ? TargetSpec.benign(TargetPredicates.spellOnStack())
                : TargetSpec.NONE;
    }
}

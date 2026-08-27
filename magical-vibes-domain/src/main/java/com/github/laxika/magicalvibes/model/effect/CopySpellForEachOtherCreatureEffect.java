package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * Trigger descriptor for an instant or sorcery spell that targets only the source creature.
 * At resolution, the spell is copied for each other creature it could target.
 */
public record CopySpellForEachOtherCreatureEffect(
        StackEntry spellSnapshot,
        UUID castingPlayerId,
        UUID originalTargetId
) implements CardEffect {

    /** Card-definition constructor; the snapshot is populated when the trigger is collected. */
    public CopySpellForEachOtherCreatureEffect() {
        this(null, null, null);
    }
}

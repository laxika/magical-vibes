package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * Resolution form of an ability that lets its controller choose creatures and pay a cost for each
 * one before copying a spell onto those creatures.
 */
public record CopySpellForEachOtherCreatureWithManaEffect(
        StackEntry spellSnapshot,
        UUID castingPlayerId,
        UUID originalTargetId,
        String manaCost
) implements CardEffect {

    /** Card-definition form; the cast snapshot is populated by the spell-cast trigger collector. */
    public CopySpellForEachOtherCreatureWithManaEffect(String manaCost) {
        this(null, null, null, manaCost);
    }

    /** Trigger-resolution form. */
    public CopySpellForEachOtherCreatureWithManaEffect(StackEntry spellSnapshot,
                                                       UUID castingPlayerId,
                                                       UUID originalTargetId,
                                                       String manaCost) {
        this.spellSnapshot = spellSnapshot;
        this.castingPlayerId = castingPlayerId;
        this.originalTargetId = originalTargetId;
        this.manaCost = manaCost;
    }
}

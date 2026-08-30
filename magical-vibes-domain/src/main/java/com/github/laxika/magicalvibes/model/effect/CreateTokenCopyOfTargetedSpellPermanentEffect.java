package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

/**
 * Resolution form of a trigger that copies one of the qualifying permanents targeted by a
 * snapshotted instant or sorcery spell. The card-definition form is the no-argument trigger
 * marker; the spell snapshot is populated when the trigger is put on the stack.
 */
public record CreateTokenCopyOfTargetedSpellPermanentEffect(StackEntry spellSnapshot)
        implements CardEffect {

    public CreateTokenCopyOfTargetedSpellPermanentEffect() {
        this(null);
    }
}

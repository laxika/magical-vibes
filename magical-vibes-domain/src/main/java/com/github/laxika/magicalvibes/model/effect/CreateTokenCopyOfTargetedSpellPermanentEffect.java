package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * Resolution form of a trigger that copies one of the qualifying permanents targeted by a
 * snapshotted instant or sorcery spell. The card-definition form is the no-argument trigger
 * marker; the spell snapshot is populated when the trigger is put on the stack.
 */
public record CreateTokenCopyOfTargetedSpellPermanentEffect(
        StackEntry spellSnapshot,
        StackEntryPredicate triggerCondition,
        CreateTokenCopyOfTargetPermanentEffect copyEffect,
        boolean instantOrSorceryOnly) implements CardEffect {

    public CreateTokenCopyOfTargetedSpellPermanentEffect {
        if (copyEffect == null) {
            copyEffect = new CreateTokenCopyOfTargetPermanentEffect();
        }
    }

    public CreateTokenCopyOfTargetedSpellPermanentEffect() {
        this(null, null, new CreateTokenCopyOfTargetPermanentEffect(), true);
    }

    public CreateTokenCopyOfTargetedSpellPermanentEffect(StackEntry spellSnapshot) {
        this(spellSnapshot, null, new CreateTokenCopyOfTargetPermanentEffect(), true);
    }

    /** Creates a trigger marker for spells matching {@code triggerCondition}. */
    public CreateTokenCopyOfTargetedSpellPermanentEffect(
            StackEntryPredicate triggerCondition, CreateTokenCopyOfTargetPermanentEffect copyEffect) {
        this(null, triggerCondition, copyEffect, false);
    }

    public CreateTokenCopyOfTargetedSpellPermanentEffect(
            StackEntry spellSnapshot, CreateTokenCopyOfTargetPermanentEffect copyEffect) {
        this(spellSnapshot, null, copyEffect, false);
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * Trigger descriptor for Exterminator Magmarch's single-copy spell trigger.
 * The empty form is the card-definition marker; the populated form is the
 * snapshot placed on the stack when the trigger is collected.
 */
public record CopySpellForAnotherOpponentPermanentEffect(
        StackEntry spellSnapshot,
        UUID castingPlayerId,
        UUID originalTargetId,
        UUID originalTargetControllerId
) implements CardEffect {

    public CopySpellForAnotherOpponentPermanentEffect() {
        this(null, null, null, null);
    }
}

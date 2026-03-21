package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * Resolution-time effect that creates a single copy of a spell for its controller.
 * The copy's controller may choose new targets.
 *
 * <p>Populated at trigger time by {@code TriggerCollectionService} when a player
 * in {@code GameData.playersWithSpellCopyUntilEndOfTurn} casts an instant or sorcery.
 * The snapshot preserves the spell's state at cast time.</p>
 *
 * <p>Used by The Mirari Conjecture chapter III's delayed triggered ability.</p>
 *
 * @param spellSnapshot snapshot of the spell on the stack at trigger time
 * @param castingPlayerId the player who cast the spell (and controls the copy)
 */
public record CopyControllerCastSpellEffect(
        StackEntry spellSnapshot,
        UUID castingPlayerId
) implements CardEffect {
}

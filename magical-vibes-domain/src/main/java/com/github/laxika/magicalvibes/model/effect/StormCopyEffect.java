package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * Resolution-time effect for the Storm keyword: creates {@code copies} copies of a spell for its
 * controller, who may choose new targets for each copy.
 *
 * <p>Populated at trigger time by {@code TriggerCollectionService} from a {@link StormEffect} on a
 * just-cast spell; {@code copies} is the number of spells cast before it this turn. The snapshot
 * preserves the spell's state at cast time.</p>
 *
 * @param spellSnapshot   snapshot of the spell on the stack at trigger time
 * @param castingPlayerId the player who cast the spell (and controls the copies)
 * @param copies          number of copies to create
 */
public record StormCopyEffect(
        StackEntry spellSnapshot,
        UUID castingPlayerId,
        int copies
) implements CardEffect {
}

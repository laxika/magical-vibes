package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * Resolution-time effect for a counted spell-copy trigger: creates {@code copies} copies of a spell
 * for its controller, who may choose new targets for each copy.
 *
 * <p>Populated at trigger time by {@code TriggerCollectionService} from a {@link StormEffect} or a
 * similar spell-copy trigger on a just-cast spell; {@code copies} is fixed when the trigger is
 * collected. The snapshot preserves the spell's state at cast time.</p>
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

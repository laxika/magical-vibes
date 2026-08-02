package com.github.laxika.magicalvibes.model;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

/**
 * A mana activation that has tapped its source but not yet produced its mana, because it stopped to
 * ask the player which colour to add ("{T}: Add one mana of any colour").
 *
 * <p>{@link ManaActivation} is recorded by diffing the pool around resolution, which for these
 * abilities would measure nothing: the prompt opens during resolution and the mana only arrives when
 * the answer comes back, long after the activation call returned. Without somewhere to park the
 * before-snapshot, such an activation could not be made revertable at all, and tapping a Birds of
 * Paradise to help pay for a spell used to poison the whole cancel window — the bird stayed tapped
 * and its mana stayed floating.
 *
 * <p>So the activation parks itself here, and the colour-choice answer completes it into a real
 * {@link ManaActivation}. Only unrestricted plain-pool colour choices park: restricted mana
 * (flashback-only, creature-spells-only, …) lands in buckets the pool diff does not see, and
 * reverting it would untap the source without draining what it produced.
 *
 * @param playerId          the player who activated the mana ability
 * @param permanentId       the tapped source permanent
 * @param poolBefore        per-colour plain pool immediately before resolution
 * @param creatureManaBefore per-colour creature-mana pool immediately before resolution
 * @param deferredTriggers  triggers this activation deferred into
 *                          {@link GameData#pendingManaAbilityTriggers}
 */
public record PendingManaActivation(
        UUID playerId,
        UUID permanentId,
        EnumMap<ManaColor, Integer> poolBefore,
        EnumMap<ManaColor, Integer> creatureManaBefore,
        List<StackEntry> deferredTriggers
) {
}

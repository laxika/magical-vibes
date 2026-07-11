package com.github.laxika.magicalvibes.model;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

/**
 * One revertable mana-ability activation: a permanent that was tapped purely for mana
 * (no other costs, no side effects beyond deferred triggers). Recorded so the UI's
 * MTGO-style "cancel casting" can untap the source and drain the produced mana again,
 * as long as the mana hasn't been spent (validated against the pool at revert time).
 *
 * @param playerId           the player who activated the mana ability
 * @param permanentId        the tapped source permanent
 * @param manaAdded          mana added to the plain pool, by color
 * @param creatureManaAdded  portion of {@code manaAdded} also tagged as creature mana
 * @param deferredTriggers   trigger stack entries this activation deferred into
 *                           {@code GameData.pendingManaAbilityTriggers} (removed on revert)
 */
public record ManaActivation(
        UUID playerId,
        UUID permanentId,
        EnumMap<ManaColor, Integer> manaAdded,
        EnumMap<ManaColor, Integer> creatureManaAdded,
        List<StackEntry> deferredTriggers
) {
}

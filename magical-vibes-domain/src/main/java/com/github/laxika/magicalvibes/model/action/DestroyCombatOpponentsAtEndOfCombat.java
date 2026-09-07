package com.github.laxika.magicalvibes.model.action;

import java.util.Set;
import java.util.UUID;

/**
 * Creature whose combat opponents are destroyed when combat ends. The opponent set is captured
 * when the delayed effect is created. The boolean selects between the bidirectional history and the
 * directional set of creatures blocked by the target. Drained in
 * {@code CombatService.processEndOfCombatCombatOpponentDestructions()}.
 */
public record DestroyCombatOpponentsAtEndOfCombat(UUID creatureId, boolean onlyCreaturesBlockedByTarget,
                                                  Set<UUID> combatOpponentIds)
        implements DelayedAction {

    public DestroyCombatOpponentsAtEndOfCombat(UUID creatureId) {
        this(creatureId, false, Set.of());
    }
}

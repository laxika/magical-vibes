package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.Set;
import java.util.UUID;

/**
 * Creature whose combat opponents are destroyed when combat ends. The opponent set is captured
 * when the delayed effect is created. The boolean selects between the bidirectional history and the
 * directional set of creatures blocked by the target. Drained in
 * {@code CombatService.processEndOfCombatCombatOpponentDestructions()}, which puts the delayed
 * trigger from {@code sourceCard} on the stack under {@code controllerId}.
 */
public record DestroyCombatOpponentsAtEndOfCombat(UUID creatureId, boolean onlyCreaturesBlockedByTarget,
                                                  Set<UUID> combatOpponentIds, Card sourceCard,
                                                  UUID controllerId)
        implements DelayedAction {

    public DestroyCombatOpponentsAtEndOfCombat(UUID creatureId) {
        this(creatureId, false, Set.of(), null, null);
    }
}
